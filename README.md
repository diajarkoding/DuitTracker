# DuitTracker

Aplikasi pencatat keuangan pribadi untuk Android dengan tampilan **Neobrutalism** yang modern. Dibangun menggunakan Jetpack Compose dan Supabase sebagai backend.

<p align="center">
  <img src="docs/screenshots/dashboard.png" width="200" alt="Dashboard"/>
  <img src="docs/screenshots/statistics.png" width="200" alt="Statistics"/>
  <img src="docs/screenshots/add-transaction.png" width="200" alt="Add Transaction"/>
</p>

---

## Daftar Isi

1. [Fitur Utama](#fitur-utama)
2. [Tech Stack](#tech-stack)
3. [Persyaratan](#persyaratan)
4. [Instalasi](#instalasi)
5. [Struktur Project](#struktur-project)
6. [Kategori Transaksi](#kategori-transaksi)
7. [Komponen UI](#komponen-ui)
8. [Arsitektur](#arsitektur)
9. [Lisensi](#lisensi)

---

## Fitur Utama

### Autentikasi
- Login dan Register dengan email
- Sesi tersimpan otomatis
- Validasi input dengan pesan error yang jelas

### Manajemen Transaksi
- Tambah, edit, dan hapus transaksi
- 13 kategori lengkap dengan ikon berwarna
- Pilihan sumber dana: Tunai, Bank, E-Wallet
- Lampirkan gambar bukti (struk/nota)
- Format mata uang Rupiah otomatis

### Dashboard
- Salam personal berdasarkan waktu
- Ringkasan saldo, pemasukan, dan pengeluaran
- Tampilan transaksi per hari atau per bulan
- Monthly data dengan fitur expand/collapse
- Indikator status online/offline

### Statistik & Analisis
- Grafik pie untuk visualisasi kategori
- Filter berdasarkan bulan
- Drill-down ke transaksi per kategori
- Export ke file Excel (XLSX)

### Mode Offline
- Aplikasi tetap berfungsi tanpa internet
- Data tersimpan lokal dan sync otomatis saat online
- Pending operations tracking

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
| **Ktor Client** | HTTP Client untuk Supabase |
| **WorkManager** | Background sync |
| **DataStore** | Preferences management |
| **Coil** | Image loading |
| **Vico Charts** | Visualisasi chart |
| **Apache POI** | Export Excel |
| **Kotlinx Serialization** | JSON parsing |
| **Kotlinx DateTime** | Date/time handling |

---

## Persyaratan

- Android Studio Ladybug atau lebih baru
- JDK 11+
- Android SDK 36
- Min SDK: 26 (Android 8.0)
- Target SDK: 36 (Android 15)
- Akun Supabase (gratis)

---

## Instalasi

### 1. Clone Repository

```bash
git clone <repository-url>
cd DuitTracker
```

### 2. Setup Database Supabase

Ikuti panduan lengkap di [docs/setup-database.md](docs/setup-database.md)

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
├── data/                       # Layer Data
│   ├── local/                  # Room Database & DataStore Preferences
│   │   ├── dao/                # Data Access Objects
│   │   ├── entity/             # Database Entities
│   │   └── preferences/        # Sync Preferences
│   ├── mapper/                 # Entity-Model Mappers
│   ├── model/                  # Data Models (Transaction, User)
│   ├── network/                # Network Monitor
│   ├── remote/                 # Supabase DTOs
│   ├── repository/             # Repository Implementations
│   └── sync/                   # Background Sync (WorkManager)
├── domain/                     # Layer Domain
│   ├── model/                  # Business Models
│   └── repository/             # Repository Interfaces
├── di/                         # Dependency Injection (Hilt Modules)
├── ui/                         # Layer Presentasi
│   ├── components/             # Komponen UI Reusable
│   ├── features/               # Screen per Fitur
│   │   ├── auth/               # Login & Register
│   │   ├── categorytransactions/ # Transaksi per Kategori
│   │   ├── dashboard/          # Dashboard Utama
│   │   ├── detail/             # Detail Transaksi
│   │   ├── edit/               # Edit Transaksi
│   │   ├── input/              # Tambah Transaksi
│   │   ├── splash/             # Splash Screen
│   │   └── statistics/         # Statistik & Charts
│   ├── navigation/             # Navigasi (Type-safe Routes)
│   └── theme/                  # Tema & Styling
└── utils/                      # Helper & Utilities
```

---

## Kategori Transaksi

### Pengeluaran

| Kategori | Warna |
|----------|-------|
| Makanan (Food) | Orange `#F97316` |
| Transportasi (Transport) | Blue `#3B82F6` |
| Belanja (Shopping) | Pink `#EC4899` |
| Hiburan (Entertainment) | Purple `#8B5CF6` |
| Tagihan (Bills) | Red `#EF4444` |
| Kesehatan (Health) | Teal `#14B8A6` |
| Pendidikan (Education) | Yellow `#EAB308` |
| Sosial (Social) | Hot Pink `#EC4899` |
| Hadiah (Gift) | Magenta `#D946EF` |
| Kebutuhan Harian (Daily Needs) | Sky Blue `#0EA5E9` |
| Lainnya (Other) | Gray `#6B7280` |

### Pemasukan

| Kategori | Warna |
|----------|-------|
| Gaji (Salary) | Green `#22C55E` |
| Investasi (Investment) | Purple `#8B5CF6` |
| Lainnya (Other) | Gray `#6B7280` |

---

## Komponen UI

Aplikasi menggunakan gaya desain **Neobrutalism** dengan komponen kustom:

| Komponen | Fungsi |
|----------|--------|
| `NeoCard` | Card dengan border dan shadow |
| `NeoCardFlat` | Card tanpa shadow |
| `NeoButton` | Tombol dengan animasi |
| `NeoIconButton` | Tombol ikon |
| `NeoInput` | Input text field |
| `NeoCurrencyInput` | Input dengan format Rupiah |
| `NeoToggle` | Toggle switch |
| `NeoTopBar` | Top app bar |
| `NeoSnackbar` | Notifikasi berwarna |
| `NeoSkeleton` | Loading placeholder |
| `OfflineIndicator` | Indikator status offline |

### Palet Warna

| Warna | Kode | Penggunaan |
|-------|------|------------|
| Electric Blue | `#2563EB` | Primary |
| Lime Green | `#22C55E` | Pemasukan |
| Expense Red | `#EF4444` | Pengeluaran |
| Sun Yellow | `#FFE500` | Aksen |
| Off White | `#FAFAF9` | Background |
| Pure Black | `#000000` | Border & Shadow |

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
Splash ────► Login ◄────► Register
               │
               ▼
          Dashboard
          /   │   \
         ▼    ▼    ▼
     Stats  Add   Detail ───► Edit
       │
       ▼
  CategoryTransactions ───► Detail
```

---

## Arsitektur

Aplikasi menggunakan **Clean Architecture** dengan 3 layer:

1. **Data Layer** - Repository implementations, Room database, Supabase integration
2. **Domain Layer** - Repository interfaces, business models
3. **Presentation Layer** - Jetpack Compose UI, ViewModels

### Offline-First Approach

- Semua transaksi disimpan ke Room database terlebih dahulu
- Background sync menggunakan WorkManager untuk sinkronisasi dengan Supabase
- NetworkMonitor untuk mendeteksi status konektivitas
- PendingOperations table untuk tracking operasi yang belum tersinkronisasi

---

## Lisensi

Hak Cipta © 2025 DuitTracker

---

Dibuat menggunakan Kotlin, Jetpack Compose, dan Supabase
