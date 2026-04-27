const express = require('express');
const multer = require('multer');
const cors = require('cors');
const path = require('path');
const fs = require('fs');
const db = require('./database');

const app = express();
const PORT = process.env.PORT || 3000;

// Setup upload directory
const uploadDir = path.join(__dirname, 'uploads');
if (!fs.existsSync(uploadDir)) {
    fs.mkdirSync(uploadDir);
}

// Setup multer storage
const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, uploadDir);
    },
    filename: function (req, file, cb) {
        cb(null, Date.now() + '-' + Math.round(Math.random() * 1E9) + path.extname(file.originalname));
    }
});
const upload = multer({ storage: storage });

app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use('/uploads', express.static(uploadDir));

app.post('/api/qc-submit', upload.array('photos'), (req, res) => {
    try {
        const sessionData = JSON.parse(req.body.sessionData);
        const defectsData = JSON.parse(req.body.defectsData);
        const files = req.files || [];

        const latitude = sessionData.latitude || null;
        const longitude = sessionData.longitude || null;

        // Begin Transaction equivalent using serialize
        db.serialize(() => {
            // 1. Insert Session
            db.run(
                `INSERT INTO qc_sessions (round, nik, operator_name, latitude, longitude) VALUES (?, ?, ?, ?, ?)`,
                [sessionData.round, sessionData.nik, sessionData.operator_name, latitude, longitude],
                function(err) {
                    if (err) {
                        console.error(err);
                        return res.status(500).json({ error: 'Failed to insert session' });
                    }
                    
                    const sessionId = this.lastID;

                    // 2. Insert Defects
                    const defectStmt = db.prepare(`
                        INSERT INTO qc_defects (session_id, line, defect_type, defect_area, qty_inspect, qty_defect) 
                        VALUES (?, ?, ?, ?, ?, ?)
                    `);

                    const photoStmt = db.prepare(`
                        INSERT INTO qc_photos (defect_id, photo_path) 
                        VALUES (?, ?)
                    `);

                    defectsData.forEach(defect => {
                        defectStmt.run(
                            [sessionId, defect.line, defect.defect_type, defect.defect_area, defect.qty_inspect, defect.qty_defect],
                            function(err) {
                                if (err) {
                                    console.error(err);
                                    return; // Note: proper rollback handling should be added for production
                                }
                                
                                const defectId = this.lastID;

                                // 3. Insert Photos for this defect
                                // Ensure defect.photo_indexes is an array
                                if (defect.photo_indexes && Array.isArray(defect.photo_indexes)) {
                                    defect.photo_indexes.forEach(index => {
                                        const file = files[index];
                                        if (file) {
                                            photoStmt.run([defectId, file.filename]);
                                        }
                                    });
                                }
                            }
                        );
                    });

                    defectStmt.finalize();
                    photoStmt.finalize();

                    res.status(200).json({ message: 'QC Data submitted successfully', session_id: sessionId });
                }
            );
        });

    } catch (error) {
        console.error('Error processing request:', error);
        res.status(400).json({ error: 'Invalid request format' });
    }
});

app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});
