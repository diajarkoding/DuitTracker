# DuitTracker

Aplikasi pencatat keuangan pribadi untuk Android dengan tampilan **Neobrutalism** yang modern. Dibangun menggunakan Jetpack Compose dan Supabase sebagai backend.

---

## Tampilan Aplikasi

| Splash | Login | Dashboard |
|--------|-------|-----------|
| Logo animasi + Cek sesi | Form login + Versi app | Ringkasan + Daftar transaksi |

| Tambah Transaksi | Detail | Statistik |
|------------------|--------|-----------|
| Form lengkap + Gambar | Info + Edit/Hapus | Chart + Export Excel |

---

## Fitur Utama

### Autentikasi
- Login dan Register dengan email
- Sesi tersimpan otomatis (tidak perlu login ulang)
- Password visibility toggle
- Validasi input dengan pesan error yang jelas

### Manajemen Transaksi
- Tambah transaksi pemasukan dan pengeluaran
- Edit transaksi yang sudah ada
- Hapus transaksi (termasuk gambar terkait)
- Kategori lengkap dengan ikon warna-warni
- Pilihan sumber dana: Tunai, Bank, E-Wallet
- Lampirkan gambar bukti (struk/nota) dari kamera atau galeri
- Format mata uang Rupiah otomatis (Rp 1.000.000)

### Dashboard
- Salam personal berdasarkan waktu dan nama pengguna
- Ringkasan saldo, pemasukan, dan pengeluaran bulan ini
- Tampilan transaksi per hari atau per bulan
- Indikator status online/offline
- Auto-refresh setelah tambah/edit/hapus

### Statistik
- Grafik pie untuk visualisasi kategori
- Filter berdasarkan bulan
- Breakdown detail per kategori
- Export ke file Excel (XLSX) dengan 6 sheet lengkap

### Mode Offline
- Aplikasi tetap bisa digunakan tanpa internet
- Data tersimpan lokal dan otomatis sync saat online
- Indikator visual saat offline
- Pending queue untuk operasi yang belum tersinkronisasi

---

## Desain Neobrutalism

Aplikasi menggunakan gaya desain Neobrutalism yang khas:

- **Border tebal** hitam pada semua komponen
- **Shadow solid** tanpa blur (hard shadow)
- **Warna cerah** namun tetap profesional
- **Tipografi tebal** untuk heading

### Palet Warna

| Warna | Kode | Penggunaan |
|-------|------|------------|
| Electric Blue | `#2563EB` | Primary, tombol utama |
| Lime Green | `#22C55E` | Pemasukan, sukses |
| Expense Red | `#EF4444` | Pengeluaran, error |
| Sun Yellow | `#FFE500` | Aksen, highlight |
| Off White | `#FAFAF9` | Background |

---

## Tech Stack

| Teknologi | Versi | Fungsi |
|-----------|-------|--------|
| Kotlin | 1.9+ | Bahasa pemrograman |
| Jetpack Compose | BOM | UI Framework modern |
| Material3 | Latest | Komponen UI |
| Hilt | 2.51 | Dependency Injection |
| Room | 2.6.1 | Database lokal |
| Supabase | 3.0.3 | Backend (Auth + DB + Storage) |
| WorkManager | 2.9.1 | Background sync |
| Coil | 2.5.0 | Image loading |
| Apache POI | 5.2.5 | Export Excel |
| Vico | 2.0.0-beta | Chart/Grafik |

---

## Persyaratan Sistem

- Android Studio Ladybug atau lebih baru
- JDK 11+
- Android SDK 36
- Min SDK: 26 (Android 8.0)
- Akun Supabase (gratis)

---

## Cara Instalasi

### 1. Clone Repository

```bash
git clone <repository-url>
cd DuitTracker
```

### 2. Setup Supabase

Ikuti panduan lengkap di [docs/BACKEND_SETUP.md](docs/BACKEND_SETUP.md)

Ringkasan singkat:
1. Buat project di [supabase.com](https://supabase.com)
2. Buat tabel `transactions` (lihat [docs/DATABASE_SCHEMA.md](docs/DATABASE_SCHEMA.md))
3. Aktifkan Row Level Security
4. Buat storage bucket `receipts`
5. Copy Project URL dan Anon Key

### 3. Konfigurasi Lokal

Buat atau edit file `local.properties` di root project:

```properties
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key-here
```

### 4. Build dan Run

```bash
# Sync Gradle
./gradlew build

# Run di emulator/device
./gradlew installDebug
```

Atau buka project di Android Studio dan klik tombol Run.

---

## Dokumentasi Lengkap

| Dokumen | Deskripsi |
|---------|-----------|
| [Backend Setup](docs/BACKEND_SETUP.md) | Panduan lengkap setup Supabase |
| [Database Schema](docs/DATABASE_SCHEMA.md) | Struktur tabel dan tipe data |
| [Arsitektur](docs/ARSITEKTUR.md) | Clean Architecture dan MVVM |

---

## Struktur Project

```
app/src/main/java/com/diajarkoding/duittracker/
├── data/                  # Layer Data
│   ├── local/             # Room Database & Preferences
│   ├── remote/            # Supabase DTOs
│   ├── repository/        # Repository Implementations
│   └── sync/              # Background Sync
├── domain/                # Layer Domain
│   ├── model/             # Business Models
│   └── repository/        # Repository Interfaces
├── di/                    # Dependency Injection (Hilt)
├── ui/                    # Layer Presentasi
│   ├── components/        # Komponen UI Reusable
│   ├── features/          # Screen per Fitur
│   ├── navigation/        # Navigasi
│   └── theme/             # Tema & Styling
└── utils/                 # Helper & Utilities
```

---

## Komponen UI Kustom

Aplikasi menyediakan komponen UI dengan gaya Neobrutalism:

| Komponen | Fungsi |
|----------|--------|
| `NeoCard` | Card dengan border dan shadow |
| `NeoCardFlat` | Card tanpa shadow |
| `NeoButton` | Tombol dengan animasi press |
| `NeoButtonText` | Tombol dengan label teks |
| `NeoIconButton` | Tombol dengan ikon |
| `NeoInput` | Input text field |
| `NeoPasswordInput` | Input password dengan toggle visibility |
| `NeoCurrencyInput` | Input nominal dengan format Rupiah |
| `NeoTopBar` | App bar dengan border |
| `NeoToggle` | Toggle switch |
| `NeoExpenseIncomeToggle` | Toggle pengeluaran/pemasukan |
| `NeoSnackbar` | Notifikasi dengan warna status |
| `NeoSkeleton` | Loading placeholder |
| `OfflineIndicator` | Indikator status offline |

---

## Alur Navigasi

```
Splash ──► Login ◄──► Register
              │
              ▼
         Dashboard
         /   │   \
        /    │    \
       ▼     ▼     ▼
   Stats  Add Tx  Detail
            │       │
            │       ▼
            └───► Edit Tx
```

---

## Kategori Transaksi

### Pengeluaran
| Kategori | Icon | Warna |
|----------|------|-------|
| Makanan | 🍔 | Orange |
| Transportasi | 🚗 | Blue |
| Belanja | 🛍️ | Pink |
| Hiburan | 🎬 | Purple |
| Tagihan | 📄 | Red |
| Kesehatan | 💊 | Teal |
| Pendidikan | 📚 | Yellow |
| Sosial | 👥 | Blue |
| Lainnya | ❓ | Gray |

### Pemasukan
| Kategori | Icon | Warna |
|----------|------|-------|
| Gaji | 💰 | Green |
| Investasi | 📈 | Blue |
| Lainnya | ❓ | Gray |

---

## Export Excel

Fitur export menghasilkan file XLSX dengan 6 sheet:

1. **Summary** - Ringkasan total pemasukan, pengeluaran, dan saldo
2. **Category Details** - Breakdown per kategori dengan detail harian
3. **Daily Details** - Transaksi dikelompokkan per tanggal
4. **Income** - Semua transaksi pemasukan
5. **Expense** - Semua transaksi pengeluaran
6. **All Transactions** - Semua transaksi lengkap

---

## Status Pengembangan

### Selesai
- [x] Desain sistem Neobrutalism
- [x] Autentikasi (Login, Register, Logout)
- [x] CRUD Transaksi lengkap
- [x] Upload gambar bukti transaksi
- [x] Dashboard dengan ringkasan keuangan
- [x] Statistik dengan pie chart
- [x] Export ke Excel
- [x] Mode offline dengan auto-sync
- [x] Format mata uang Rupiah
- [x] Category picker yang bisa di-collapse
- [x] Tampilan versi aplikasi

### Rencana Pengembangan
- [ ] Dark mode
- [ ] Budget bulanan dengan notifikasi
- [ ] Multi-currency
- [ ] Recurring transactions
- [ ] Backup dan restore data
- [ ] Widget home screen

---

## Troubleshooting

### Build gagal: "SDK location not found"
Pastikan file `local.properties` ada dan berisi path SDK:
```properties
sdk.dir=/path/to/Android/sdk
```

### Error: "bucket not found"
Pastikan storage bucket `receipts` sudah dibuat di Supabase.

### Gambar tidak muncul
- Periksa apakah bucket `receipts` sudah dibuat
- Pastikan policy storage sudah dikonfigurasi
- Cek log dengan tag `ImageRepository`

### Sync tidak berjalan
- Pastikan device terkoneksi internet
- Cek log dengan tag `SyncManager`
- Periksa pending operations di database lokal

---

## Kontribusi

Kontribusi sangat diterima! Silakan:
1. Fork repository ini
2. Buat branch fitur (`git checkout -b fitur-baru`)
3. Commit perubahan (`git commit -m 'Tambah fitur baru'`)
4. Push ke branch (`git push origin fitur-baru`)
5. Buat Pull Request

---

## Lisensi

Hak Cipta © 2025 DuitTracker

---

## Pengembang

Dibuat dengan Kotlin, Jetpack Compose, dan Supabase
