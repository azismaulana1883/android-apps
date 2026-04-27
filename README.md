# QC Inline Mobile App

Aplikasi **QC (Quality Control) Inline** berbasis Android Native yang dirancang untuk memudahkan proses inspeksi kualitas di lapangan (pabrik/produksi). Aplikasi ini memungkinkan operator QC untuk menginput data cacat (defect) barang secara cepat, mengambil foto bukti langsung dari kamera, serta secara otomatis mencatat lokasi inspeksi (Geolocation).

Project ini terdiri dari dua bagian utama:
1. **Frontend (Aplikasi Android):** Dibangun menggunakan Kotlin dan Jetpack Compose.
2. **Backend (Server):** Dibangun menggunakan Node.js (Express) dan database lokal SQLite.

## ✨ Fitur Utama

- **Multi-Layer Form:** Antarmuka pengisian data bertahap untuk kemudahan penggunaan (Pilih Round -> Data Operator -> Detail Defect).
- **Dynamic Defect List:** Mendukung penambahan multiple defect sekaligus dalam satu kali inspeksi.
- **Direct Camera Capture:** Tombol pengambilan foto yang langsung terintegrasi dengan Kamera HP untuk mempercepat pengambilan bukti.
- **Auto Geolocation:** Otomatis mendeteksi dan menyimpan titik Latitude & Longitude saat pengiriman data.
- **Multipart Upload:** Pengiriman data teks dan gambar secara bersamaan ke server lokal.

## 🛠️ Teknologi yang Digunakan

### Frontend (Android)
- **Bahasa:** Kotlin
- **UI Toolkit:** Jetpack Compose (Material 3)
- **Navigasi:** Compose Navigation
- **Networking:** Retrofit & OkHttp
- **Image Loading:** Coil
- **Location:** Google Play Services Location (FusedLocationProviderClient)

### Backend (Node.js)
- **Framework:** Express.js
- **Database:** SQLite3
- **File Upload:** Multer

## 🚀 Panduan Menjalankan Project

### 1. Menjalankan Backend (Server)
Pastikan Anda sudah menginstal [Node.js](https://nodejs.org/).
1. Buka terminal dan masuk ke folder `backend`.
2. Jalankan perintah `npm install` untuk mengunduh dependencies.
3. Jalankan server dengan perintah:
   ```bash
   node server.js
   ```
4. Server akan menyala di `http://localhost:3000`. Jika ingin diakses dari HP asli, pastikan HP berada di WiFi yang sama, dan ubah `BASE_URL` di Android sesuai dengan IP Address komputer Anda.

### 2. Menjalankan Aplikasi Android
1. Buka folder `QCInline` menggunakan **Android Studio**.
2. Tunggu proses *Gradle Sync* selesai.
3. Jika menggunakan emulator (seperti Medium Phone API 35), pastikan emulator sudah berjalan.
4. Klik tombol **Run (▶️)** di Android Studio untuk meng-install dan membuka aplikasi.
5. Atau, Anda bisa langsung meng-install file APK yang telah di-build (`app-debug.apk`) ke HP asli.

## 📁 Struktur Database (SQLite)
Database akan ter-generate otomatis saat backend pertama kali dijalankan. Terdapat tiga tabel utama:
- `qc_sessions`: Menyimpan data sesi inspeksi (Round, NIK, Nama, Latitude, Longitude, Waktu).
- `qc_defects`: Menyimpan detail defect (Line, Jenis Defect, Area, Qty).
- `qc_photos`: Menyimpan _path_ foto bukti defect.
