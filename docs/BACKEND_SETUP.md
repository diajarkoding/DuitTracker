# Panduan Setup Backend (Supabase)

Dokumen ini menjelaskan langkah-langkah untuk menyiapkan backend Supabase agar aplikasi DuitTracker dapat berjalan dengan baik.

---

## Daftar Isi

1. [Membuat Project Supabase](#1-membuat-project-supabase)
2. [Mengaktifkan Autentikasi Email](#2-mengaktifkan-autentikasi-email)
3. [Membuat Tabel Database](#3-membuat-tabel-database)
4. [Mengatur Row Level Security (RLS)](#4-mengatur-row-level-security-rls)
5. [Membuat Storage Bucket](#5-membuat-storage-bucket)
6. [Mengambil Kredensial API](#6-mengambil-kredensial-api)
7. [Konfigurasi di Aplikasi](#7-konfigurasi-di-aplikasi)

---

## 1. Membuat Project Supabase

1. Buka [supabase.com](https://supabase.com) dan login atau daftar akun baru
2. Klik tombol **"New Project"**
3. Isi informasi project:
   - **Name**: `DuitTracker` (atau nama lain sesuai keinginan)
   - **Database Password**: Buat password yang kuat dan simpan baik-baik
   - **Region**: Pilih region terdekat (misal: Singapore untuk pengguna Indonesia)
4. Klik **"Create new project"** dan tunggu hingga setup selesai (biasanya 1-2 menit)

---

## 2. Mengaktifkan Autentikasi Email

DuitTracker menggunakan autentikasi email dan password. Berikut cara mengaktifkannya:

1. Di dashboard Supabase, buka menu **Authentication** > **Providers**
2. Pastikan **Email** sudah dalam status **Enabled**
3. Pengaturan yang direkomendasikan:
   - **Confirm email**: Bisa dinonaktifkan untuk development, aktifkan untuk production
   - **Secure email change**: Aktifkan untuk keamanan tambahan

### Catatan Penting

Jika "Confirm email" diaktifkan, pengguna perlu mengklik link konfirmasi yang dikirim ke email mereka sebelum bisa login. Untuk testing, sebaiknya nonaktifkan dulu fitur ini.

---

## 3. Membuat Tabel Database

Buka menu **SQL Editor** di dashboard Supabase, lalu jalankan query berikut:

```sql
-- Tabel untuk menyimpan data transaksi
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

-- Index untuk mempercepat query berdasarkan user_id
CREATE INDEX idx_transactions_user_id ON transactions(user_id);

-- Index untuk mempercepat query berdasarkan tanggal
CREATE INDEX idx_transactions_date ON transactions(transaction_date DESC);

-- Trigger untuk auto-update kolom updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_transactions_updated_at
    BEFORE UPDATE ON transactions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

---

## 4. Mengatur Row Level Security (RLS)

Row Level Security memastikan setiap pengguna hanya bisa mengakses data miliknya sendiri. Jalankan query berikut:

```sql
-- Aktifkan RLS pada tabel transactions
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;

-- Policy: Pengguna hanya bisa melihat transaksi miliknya
CREATE POLICY "Users can view own transactions"
    ON transactions FOR SELECT
    USING (auth.uid() = user_id);

-- Policy: Pengguna hanya bisa menambah transaksi untuk dirinya
CREATE POLICY "Users can insert own transactions"
    ON transactions FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- Policy: Pengguna hanya bisa mengubah transaksi miliknya
CREATE POLICY "Users can update own transactions"
    ON transactions FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- Policy: Pengguna hanya bisa menghapus transaksi miliknya
CREATE POLICY "Users can delete own transactions"
    ON transactions FOR DELETE
    USING (auth.uid() = user_id);
```

---

## 5. Membuat Storage Bucket

Storage bucket digunakan untuk menyimpan gambar bukti transaksi (struk, nota, dll).

1. Buka menu **Storage** di dashboard Supabase
2. Klik **"New bucket"**
3. Isi informasi bucket:
   - **Name**: `receipts`
   - **Public bucket**: **Jangan dicentang** (biarkan private)
4. Klik **"Create bucket"**

### Mengatur Policy Storage

Setelah bucket dibuat, atur policy agar pengguna hanya bisa mengakses file miliknya:

```sql
-- Policy: Pengguna bisa upload file ke folder miliknya
CREATE POLICY "Users can upload own receipts"
    ON storage.objects FOR INSERT
    WITH CHECK (
        bucket_id = 'receipts' AND
        auth.uid()::text = (storage.foldername(name))[1]
    );

-- Policy: Pengguna bisa melihat file miliknya
CREATE POLICY "Users can view own receipts"
    ON storage.objects FOR SELECT
    USING (
        bucket_id = 'receipts' AND
        auth.uid()::text = (storage.foldername(name))[1]
    );

-- Policy: Pengguna bisa menghapus file miliknya
CREATE POLICY "Users can delete own receipts"
    ON storage.objects FOR DELETE
    USING (
        bucket_id = 'receipts' AND
        auth.uid()::text = (storage.foldername(name))[1]
    );
```

---

## 6. Mengambil Kredensial API

Untuk menghubungkan aplikasi dengan Supabase, kamu memerlukan dua informasi:

1. Buka menu **Settings** > **API**
2. Catat informasi berikut:
   - **Project URL**: `https://xxxxx.supabase.co`
   - **anon public key**: String panjang (API key)

### Perbedaan API Keys

| Key | Fungsi | Keamanan |
|-----|--------|----------|
| `anon` / `public` | Untuk akses dari client (aplikasi) | Aman digunakan di aplikasi, dilindungi RLS |
| `service_role` | Akses penuh tanpa RLS | **JANGAN** gunakan di aplikasi client! |

---

## 7. Konfigurasi di Aplikasi

Setelah mendapatkan kredensial, tambahkan ke file `local.properties` di root project:

```properties
# Supabase Configuration
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_ANON_KEY=your-anon-key-here
```

### Catatan Keamanan

- File `local.properties` sudah termasuk dalam `.gitignore`, jadi tidak akan ter-commit ke repository
- **Jangan pernah** membagikan `service_role` key
- Untuk production, pertimbangkan menggunakan environment variables atau secret management

---

## Troubleshooting

### Error: "new row violates row-level security policy"

Ini berarti RLS sedang bekerja dengan benar, tapi ada masalah dengan:
- User belum login
- Mencoba mengakses data milik user lain
- `user_id` tidak cocok dengan `auth.uid()`

### Error: "bucket not found"

Pastikan:
- Nama bucket sudah benar (`receipts`)
- Bucket sudah dibuat di Supabase Storage

### Error: "Invalid API key"

Pastikan:
- Menggunakan `anon` key, bukan `service_role`
- Key sudah di-copy dengan benar (tidak ada spasi/karakter tambahan)
- Project URL sudah benar

---

## Referensi

- [Dokumentasi Supabase](https://supabase.com/docs)
- [Supabase Auth Guide](https://supabase.com/docs/guides/auth)
- [Row Level Security Guide](https://supabase.com/docs/guides/auth/row-level-security)
- [Storage Guide](https://supabase.com/docs/guides/storage)
