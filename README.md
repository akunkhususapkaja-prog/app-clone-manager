# App Clone Manager

Aplikasi Android untuk mengkloning/menduplikasi aplikasi, menginstal APK ke virtual space, dan akses Play Store — tanpa iklan dan tanpa root.

## Fitur

- **Clone App** — Duplikasi aplikasi terinstal (multi-instance)
- **APK Installer** — Instal APK dari file manager
- **Play Store** — Akses dan instal dari Play Store
- **Managed Apps** — Kelola semua clone
- **Tanpa iklan** — Aman dan privat

## Cara Build APK (Via GitHub Actions)

### Langkah 1: Upload ke GitHub
1. Buat akun di https://github.com
2. Buat repository baru (public)
3. Upload semua file dari project ini ke repository

### Langkah 2: Build APK Otomatis
1. Buka repository di GitHub (dari browser HP)
2. Klik tab **"Actions"**
3. Klik **"Build APK"** di sebelah kiri
4. Klik **"Run workflow"** → **"Run workflow"** (tombol hijau)
5. Tunggu 10-20 menit
6. Klik hasil build terakhir
7. Download **"AppCloneManager-v1.0.0-debug"** artifact
8. Install APK di HP Anda

### Atau: Build Manual di Laptop
1. Install Android Studio di laptop
2. Buka folder project
3. Klik **Build → Build APK**
4. APK ada di `app/build/outputs/apk/debug/app-debug.apk`

## Catatan
- APK yang dihasilkan **tidak di-sign** (debug build) — perlu diaktifkan "Install from unknown sources" di HP
- Untuk production, perlu di-sign dengan keystore
