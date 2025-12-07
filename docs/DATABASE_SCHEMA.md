# Struktur Database

Dokumen ini menjelaskan struktur database yang digunakan oleh aplikasi DuitTracker, baik di sisi lokal (Room) maupun remote (Supabase).

---

## Daftar Isi

1. [Gambaran Umum](#gambaran-umum)
2. [Tabel Transactions](#tabel-transactions)
3. [Tabel Pending Operations](#tabel-pending-operations)
4. [Enum dan Tipe Data](#enum-dan-tipe-data)
5. [Sinkronisasi Data](#sinkronisasi-data)

---

## Gambaran Umum

DuitTracker menggunakan arsitektur **Remote-First dengan Offline Fallback**:

```
┌─────────────────────────────────────────┐
│           Supabase (Remote)             │
│  - Sumber data utama saat online        │
│  - PostgreSQL dengan RLS                │
└─────────────────┬───────────────────────┘
                  │
                  │ Sync
                  ▼
┌─────────────────────────────────────────┐
│           Room Database (Local)         │
│  - Cache data untuk offline             │
│  - Menyimpan operasi pending            │
│  - SQLite di perangkat                  │
└─────────────────────────────────────────┘
```

---

## Tabel Transactions

Tabel utama yang menyimpan semua data transaksi pengguna.

### Struktur Kolom

| Kolom | Tipe (Room) | Tipe (Supabase) | Deskripsi |
|-------|-------------|-----------------|-----------|
| `id` | TEXT (PK) | UUID (PK) | ID unik transaksi |
| `user_id` | TEXT | UUID (FK) | ID pengguna pemilik transaksi |
| `amount` | REAL | DECIMAL(15,2) | Nominal transaksi |
| `category` | TEXT | TEXT | Kategori transaksi |
| `type` | TEXT | TEXT | Tipe: `expense` atau `income` |
| `account_source` | TEXT | TEXT | Sumber: `cash`, `bank`, `ewallet` |
| `note` | TEXT | TEXT | Catatan singkat |
| `description` | TEXT? | TEXT | Deskripsi detail (opsional) |
| `image_path` | TEXT? | TEXT | Path gambar bukti (opsional) |
| `transaction_date` | TEXT | TIMESTAMPTZ | Tanggal transaksi |
| `is_synced` | INTEGER | - | Status sinkronisasi (hanya lokal) |
| `created_at` | TEXT | TIMESTAMPTZ | Waktu data dibuat |
| `updated_at` | TEXT | TIMESTAMPTZ | Waktu data terakhir diubah |

### SQL Schema (Room)

```sql
CREATE TABLE transactions (
    id TEXT PRIMARY KEY NOT NULL,
    user_id TEXT NOT NULL,
    amount REAL NOT NULL,
    category TEXT NOT NULL,
    type TEXT NOT NULL,
    account_source TEXT NOT NULL,
    note TEXT NOT NULL,
    description TEXT,
    image_path TEXT,
    transaction_date TEXT NOT NULL,
    is_synced INTEGER NOT NULL DEFAULT 0,
    created_at TEXT,
    updated_at TEXT
);
```

### SQL Schema (Supabase)

```sql
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    category TEXT NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('expense', 'income')),
    account_source TEXT NOT NULL CHECK (account_source IN ('cash', 'bank', 'ewallet')),
    note TEXT DEFAULT '',
    description TEXT,
    image_path TEXT,
    transaction_date TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
```

---

## Tabel Pending Operations

Tabel ini hanya ada di database lokal (Room) untuk menyimpan operasi yang belum berhasil disinkronkan ke server.

### Struktur Kolom

| Kolom | Tipe | Deskripsi |
|-------|------|-----------|
| `id` | TEXT (PK) | ID unik operasi |
| `operation_type` | TEXT | Tipe operasi: `INSERT`, `UPDATE`, `DELETE` |
| `entity_id` | TEXT | ID entity yang terpengaruh |
| `payload` | TEXT | Data dalam format JSON |
| `created_at` | TEXT | Waktu operasi dibuat |
| `retry_count` | INTEGER | Jumlah percobaan ulang |

### SQL Schema

```sql
CREATE TABLE pending_operations (
    id TEXT PRIMARY KEY NOT NULL,
    operation_type TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    payload TEXT NOT NULL,
    created_at TEXT NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0
);
```

### Catatan Penting

- Operasi dengan `retry_count >= 3` akan otomatis dihapus
- Saat koneksi tersedia, aplikasi akan mencoba sinkronisasi otomatis
- Untuk operasi DELETE dengan gambar, payload berformat: `transaction_id|image_path`

---

## Enum dan Tipe Data

### TransactionType

Menentukan apakah transaksi merupakan pengeluaran atau pemasukan.

| Nilai | Deskripsi |
|-------|-----------|
| `expense` | Pengeluaran (uang keluar) |
| `income` | Pemasukan (uang masuk) |

### TransactionCategory

Kategori untuk mengklasifikasikan transaksi.

**Kategori Pengeluaran:**

| Nilai | Label | Icon | Warna |
|-------|-------|------|-------|
| `food` | Makanan | 🍔 | Orange |
| `transport` | Transportasi | 🚗 | Blue |
| `shopping` | Belanja | 🛍️ | Pink |
| `entertainment` | Hiburan | 🎬 | Purple |
| `bills` | Tagihan | 📄 | Red |
| `health` | Kesehatan | 💊 | Teal |
| `education` | Pendidikan | 📚 | Yellow |
| `social` | Sosial | 👥 | Blue |
| `other` | Lainnya | ❓ | Gray |

**Kategori Pemasukan:**

| Nilai | Label | Icon | Warna |
|-------|-------|------|-------|
| `salary` | Gaji | 💰 | Green |
| `investment` | Investasi | 📈 | Blue |
| `other` | Lainnya | ❓ | Gray |

### AccountSource

Sumber dana transaksi.

| Nilai | Label | Deskripsi |
|-------|-------|-----------|
| `cash` | Tunai | Uang cash fisik |
| `bank` | Bank | Transfer atau debit bank |
| `ewallet` | E-Wallet | Dompet digital (GoPay, OVO, dll) |

---

## Sinkronisasi Data

### Alur Sinkronisasi

```
┌──────────────────────────────────────────────────────────────┐
│                      OPERASI CRUD                             │
└──────────────────────────────┬───────────────────────────────┘
                               │
                  ┌────────────┴────────────┐
                  ▼                         ▼
            ┌─────────┐               ┌──────────┐
            │ ONLINE  │               │ OFFLINE  │
            └────┬────┘               └────┬─────┘
                 │                         │
                 ▼                         ▼
    ┌────────────────────┐    ┌──────────────────────────┐
    │ Simpan ke Supabase │    │ Simpan ke Room +         │
    │ + Cache ke Room    │    │ Pending Operations Queue │
    └────────────────────┘    └──────────────────────────┘
                                           │
                               Saat koneksi tersedia
                                           │
                                           ▼
                              ┌───────────────────────┐
                              │ Auto-sync ke Supabase │
                              │ + Hapus dari pending  │
                              └───────────────────────┘
```

### Status Sinkronisasi (`is_synced`)

| Nilai | Arti |
|-------|------|
| `0` (false) | Data belum disinkronkan ke server |
| `1` (true) | Data sudah tersinkronisasi dengan server |

### Penanganan Konflik

Karena menggunakan arsitektur Remote-First:
- Saat online, data langsung disimpan ke server (tidak ada konflik)
- Saat offline, data disimpan lokal dan akan di-push saat online
- Jika push gagal 3x, operasi dianggap gagal dan dihapus

### Penghapusan Bertingkat

Saat transaksi dihapus:
1. Hapus gambar dari Supabase Storage (jika ada)
2. Hapus data dari tabel transactions di Supabase
3. Hapus cache dari Room Database
4. Hapus gambar cache lokal (jika ada)

---

## Index dan Performa

### Index yang Digunakan

```sql
-- Mempercepat query berdasarkan user
CREATE INDEX idx_transactions_user_id ON transactions(user_id);

-- Mempercepat query berdasarkan tanggal (untuk dashboard)
CREATE INDEX idx_transactions_date ON transactions(transaction_date DESC);
```

### Tips Optimasi

1. **Gunakan Pagination**: Untuk daftar transaksi yang panjang
2. **Filter di Server**: Kirim parameter filter ke Supabase, bukan filter di client
3. **Cache Selektif**: Hanya cache data yang diperlukan (misal: 3 bulan terakhir)
