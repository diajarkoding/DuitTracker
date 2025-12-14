# DuitTracker

Aplikasi pencatat keuangan pribadi untuk Android dengan tampilan **Neobrutalism** yang modern. Dibangun menggunakan Jetpack Compose dan Supabase sebagai backend.

---

## Daftar Isi

1. [Fitur Utama](#fitur-utama)
2. [Tech Stack](#tech-stack)
3. [Persyaratan](#persyaratan)
4. [Instalasi](#instalasi)
5. [Struktur Project](#struktur-project)
6. [Kategori Transaksi](#kategori-transaksi)
7. [Komponen UI](#komponen-ui)
8. [Lisensi](#lisensi)

---

## Fitur Utama

### 🔐 Autentikasi
- Login dan Register dengan email
- Sesi tersimpan otomatis
- Validasi input dengan pesan error yang jelas

### 💰 Manajemen Transaksi
- Tambah, edit, dan hapus transaksi
- Kategori lengkap dengan ikon berwarna
- Pilihan sumber dana: Tunai, Bank, E-Wallet
- Lampirkan gambar bukti (struk/nota)
- Format mata uang Rupiah otomatis

### 📊 Dashboard
- Salam personal berdasarkan waktu
- Ringkasan saldo, pemasukan, dan pengeluaran
- Tampilan transaksi per hari atau per bulan
- Indikator status online/offline

### 📈 Statistik
- Grafik pie untuk visualisasi kategori
- Filter berdasarkan bulan
- Export ke file Excel (XLSX)

### 📴 Mode Offline
- Aplikasi tetap berfungsi tanpa internet
- Data tersimpan lokal dan sync otomatis saat online

---

## Tech Stack

| Teknologi | Fungsi |
|-----------|--------|
| **Kotlin** | Bahasa pemrograman |
| **Jetpack Compose** | UI Framework |
| **Material3** | Komponen UI |
| **Hilt** | Dependency Injection |
| **Room** | Database lokal |
| **Supabase** | Backend (Auth + Database + Storage) |
| **WorkManager** | Background sync |
| **Coil** | Image loading |
| **Apache POI** | Export Excel |

---

## Persyaratan

- Android Studio Ladybug atau lebih baru
- JDK 11+
- Android SDK 36
- Min SDK: 26 (Android 8.0)
- Akun Supabase (gratis)

---

## Instalasi

### 1. Clone Repository

```bash
git clone <repository-url>
cd DuitTracker
```

### 2. Setup Database Supabase

📖 **Ikuti panduan lengkap di [docs/setup-database.md](docs/setup-database.md)**

Panduan tersebut mencakup:
- Membuat akun dan project Supabase
- Membuat tabel dan enum
- Mengaktifkan Row Level Security
- Membuat storage bucket untuk gambar
- Mendapatkan API keys

### 3. Konfigurasi Aplikasi

Edit file `local.properties` di root project:

```properties
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key-here
```

### 4. Build dan Run

```bash
./gradlew build
./gradlew installDebug
```

Atau buka project di Android Studio dan klik tombol **Run**.

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

## Kategori Transaksi

### Pengeluaran

| Kategori | Warna |
|----------|-------|
| Makanan | 🟠 Orange |
| Transportasi | 🔵 Blue |
| Belanja | 🩷 Pink |
| Hiburan | 🟣 Purple |
| Tagihan | 🔴 Red |
| Kesehatan | 🩵 Teal |
| Pendidikan | 🟡 Yellow |
| Sosial | 🩷 Hot Pink |
| Hadiah | 🟣 Magenta |
| Kebutuhan Harian | 🔵 Sky Blue |
| Lainnya | ⚫ Gray |

### Pemasukan

| Kategori | Warna |
|----------|-------|
| Gaji | 🟢 Green |
| Investasi | 🟣 Purple |
| Lainnya | ⚫ Gray |

---

## Komponen UI

Aplikasi menggunakan gaya desain **Neobrutalism** dengan komponen kustom:

| Komponen | Fungsi |
|----------|--------|
| `NeoCard` | Card dengan border dan shadow |
| `NeoCardFlat` | Card tanpa shadow |
| `NeoButton` | Tombol dengan animasi |
| `NeoInput` | Input text field |
| `NeoCurrencyInput` | Input dengan format Rupiah |
| `NeoSnackbar` | Notifikasi berwarna |
| `NeoSkeleton` | Loading placeholder |

### Palet Warna

| Warna | Kode | Penggunaan |
|-------|------|------------|
| Electric Blue | `#2563EB` | Primary |
| Lime Green | `#22C55E` | Pemasukan |
| Expense Red | `#EF4444` | Pengeluaran |
| Sun Yellow | `#FFE500` | Aksen |
| Off White | `#FAFAF9` | Background |

---

## Export Excel

Fitur export menghasilkan file XLSX dengan 6 sheet:

1. **Summary** - Ringkasan total
2. **Category Details** - Breakdown per kategori
3. **Daily Details** - Transaksi per tanggal
4. **Income** - Semua pemasukan
5. **Expense** - Semua pengeluaran
6. **All Transactions** - Semua transaksi

---

## Alur Navigasi

```
Splash ──► Login ◄──► Register
              │
              ▼
         Dashboard
         /   │   \
        ▼    ▼    ▼
    Stats  Add   Detail ──► Edit
             │
             ▼
      CategoryList ──► Detail
```

---

## Lisensi

Hak Cipta © 2025 DuitTracker

---

Dibuat dengan ❤️ menggunakan Kotlin, Jetpack Compose, dan Supabase
