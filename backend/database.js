const sqlite3 = require('sqlite3').verbose();
const path = require('path');

const dbPath = path.resolve(__dirname, 'database.db');

const db = new sqlite3.Database(dbPath, (err) => {
    if (err) {
        console.error('Error connecting to SQLite database:', err.message);
    } else {
        console.log('Connected to the SQLite database.');
        initializeTables();
    }
});

function initializeTables() {
    db.serialize(() => {
        db.run(`
            CREATE TABLE IF NOT EXISTS qc_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                round INTEGER NOT NULL,
                operator_nik TEXT NOT NULL,
                operator_name TEXT NOT NULL,
                latitude REAL,
                longitude REAL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        `);

        db.run(`
            CREATE TABLE IF NOT EXISTS qc_defects (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_id INTEGER,
                line TEXT,
                defect_type TEXT,
                defect_area TEXT,
                qty_inspect INTEGER,
                qty_defect INTEGER,
                FOREIGN KEY(session_id) REFERENCES qc_sessions(id)
            )
        `);

        db.run(`
            CREATE TABLE IF NOT EXISTS qc_photos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                defect_id INTEGER,
                photo_path TEXT,
                FOREIGN KEY(defect_id) REFERENCES qc_defects(id)
            )
        `);
        
        console.log('Database tables initialized.');
    });
}

module.exports = db;
