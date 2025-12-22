# Setup Database Supabase

Panduan lengkap untuk setup database Supabase agar bisa terintegrasi dengan aplikasi DuitTracker.

---

## Daftar Isi

1. [Membuat Akun Supabase](#1-membuat-akun-supabase)
2. [Membuat Project Baru](#2-membuat-project-baru)
3. [Membuat Tabel Transactions](#3-membuat-tabel-transactions)
4. [Membuat Tabel Profiles](#4-membuat-tabel-profiles)
5. [Mengaktifkan Row Level Security](#5-mengaktifkan-row-level-security)
6. [Membuat Storage Bucket](#6-membuat-storage-bucket)
7. [Mendapatkan API Keys](#7-mendapatkan-api-keys)
8. [Konfigurasi Aplikasi](#8-konfigurasi-aplikasi)

---

## 1. Membuat Akun Supabase

1. Buka [supabase.com](https://supabase.com)
2. Klik **Start your project** atau **Sign Up**
3. Daftar menggunakan:
   - GitHub (disarankan)
   - Email dan password
4. Verifikasi email jika menggunakan email

---

## 2. Membuat Project Baru

1. Setelah login, klik **New Project**
2. Isi informasi project:
   - **Name**: `duittracker` (atau nama lain)
   - **Database Password**: Buat password yang kuat (simpan baik-baik!)
   - **Region**: Pilih yang terdekat (contoh: Singapore)
3. Klik **Create new project**
4. Tunggu hingga project selesai dibuat (sekitar 2-3 menit)

---

## 3. Membuat Tabel Transactions

### Menggunakan SQL Editor

1. Di sidebar kiri, klik **SQL Editor**
2. Klik **New query**
3. Copy dan paste SQL berikut:

```sql
-- Buat enum untuk kategori transaksi
CREATE TYPE transaction_category AS ENUM (
    'food',
    'transport',
    'shopping',
    'entertainment',
    'bills',
    'health',
    'education',
    'social',
    'salary',
    'investment',
    'gift',
    'daily_needs',
    'other'
);

-- Buat enum untuk tipe transaksi
CREATE TYPE transaction_type AS ENUM (
    'income',
    'expense'
);

-- Buat enum untuk sumber akun (lowercase)
CREATE TYPE account_source AS ENUM (
    'cash',
    'bank',
    'ewallet'
);

-- Buat tabel transactions
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    amount NUMERIC NOT NULL CHECK (amount > 0),
    category transaction_category NOT NULL,
    type transaction_type NOT NULL,
    account_source account_source NOT NULL DEFAULT 'cash',
    note TEXT NOT NULL DEFAULT '',
    description TEXT,
    image_path TEXT,
    transaction_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Tambahkan foreign key dengan ON DELETE CASCADE
ALTER TABLE transactions
ADD CONSTRAINT transactions_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE;

-- Buat index untuk performa query
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_category ON transactions(category);
```

4. Klik **Run** (atau tekan `Ctrl+Enter`)
5. Pastikan tidak ada error

---

## 4. Membuat Tabel Profiles

Tabel ini untuk menyimpan data profil pengguna.

```sql
-- Buat tabel profiles
CREATE TABLE profiles (
    id UUID PRIMARY KEY,
    email TEXT NOT NULL,
    name TEXT NOT NULL,
    avatar_url TEXT,
    currency TEXT DEFAULT 'IDR',
    monthly_budget NUMERIC,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Tambahkan foreign key dengan ON DELETE CASCADE
ALTER TABLE profiles
ADD CONSTRAINT profiles_id_fkey
    FOREIGN KEY (id) REFERENCES auth.users(id) ON DELETE CASCADE;

-- Trigger untuk auto-create profile saat user register
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, email, name)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'name', split_part(NEW.email, '@', 1))
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();
```

---

## 5. Mengaktifkan Row Level Security

Row Level Security (RLS) memastikan setiap user hanya bisa mengakses data miliknya sendiri.

### Mengaktifkan RLS

1. Di SQL Editor, jalankan:

```sql
-- Aktifkan RLS pada tabel transactions
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;

-- Policy: User hanya bisa SELECT data miliknya
CREATE POLICY "Users can view own transactions"
ON transactions FOR SELECT
USING (auth.uid() = user_id);

-- Policy: User hanya bisa INSERT data miliknya
CREATE POLICY "Users can insert own transactions"
ON transactions FOR INSERT
WITH CHECK (auth.uid() = user_id);

-- Policy: User hanya bisa UPDATE data miliknya
CREATE POLICY "Users can update own transactions"
ON transactions FOR UPDATE
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- Policy: User hanya bisa DELETE data miliknya
CREATE POLICY "Users can delete own transactions"
ON transactions FOR DELETE
USING (auth.uid() = user_id);

-- Aktifkan RLS pada tabel profiles
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;

-- Policy: User hanya bisa view profile miliknya
CREATE POLICY "Users can view own profile"
ON profiles FOR SELECT
USING (auth.uid() = id);

-- Policy: User hanya bisa update profile miliknya
CREATE POLICY "Users can update own profile"
ON profiles FOR UPDATE
USING (auth.uid() = id)
WITH CHECK (auth.uid() = id);
```

2. Klik **Run**

### Verifikasi RLS

1. Di sidebar, klik **Table Editor**
2. Pilih tabel **transactions**
3. Klik tab **RLS**
4. Pastikan RLS **Enabled** dan ada 4 policies

---

## 6. Membuat Storage Bucket

Storage bucket digunakan untuk menyimpan gambar bukti transaksi (struk/nota).

### Membuat Bucket

1. Di sidebar kiri, klik **Storage**
2. Klik **New bucket**
3. Isi:
   - **Name**: `receipts`
   - **Public bucket**: ✅ Centang (check)
4. Klik **Create bucket**

### Mengatur Policy Storage

1. Pilih bucket **receipts**
2. Klik tab **Policies**
3. Klik **New policy** → **For full customization**
4. Buat policy berikut:

**Policy 1: Upload (INSERT)**
```sql
-- Name: Allow authenticated uploads
-- Allowed operation: INSERT
-- Policy definition:
((bucket_id = 'receipts'::text) AND (auth.role() = 'authenticated'::text))
```

**Policy 2: View (SELECT)**
```sql
-- Name: Allow public viewing
-- Allowed operation: SELECT
-- Policy definition:
(bucket_id = 'receipts'::text)
```

**Policy 3: Delete (DELETE)**
```sql
-- Name: Allow owner to delete
-- Allowed operation: DELETE
-- Policy definition:
((bucket_id = 'receipts'::text) AND (auth.role() = 'authenticated'::text))
```

Atau jalankan SQL ini di SQL Editor:

```sql
-- Storage policies
CREATE POLICY "Allow authenticated uploads"
ON storage.objects FOR INSERT
TO authenticated
WITH CHECK (bucket_id = 'receipts');

CREATE POLICY "Allow public viewing"
ON storage.objects FOR SELECT
TO public
USING (bucket_id = 'receipts');

CREATE POLICY "Allow owner to delete"
ON storage.objects FOR DELETE
TO authenticated
USING (bucket_id = 'receipts');
```

---

## 7. Mendapatkan API Keys

1. Di sidebar kiri, klik **Project Settings** (ikon gear)
2. Klik **API** di submenu
3. Catat dua nilai berikut:
   - **Project URL**: `https://xxxxx.supabase.co`
   - **anon public key**: String panjang yang dimulai dengan `eyJ...`

**Penting**: Jangan share `service_role key` di aplikasi client!
---

## 7. Konfigurasi Aplikasi
## 8. Konfigurasi Aplikasi
### Edit local.properties

1. Buka project DuitTracker di Android Studio
2. Buka file `local.properties` di root folder
3. Tambahkan baris berikut:

```properties
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Ganti dengan nilai yang Anda catat dari langkah sebelumnya.

### Build Ulang

```bash
./gradlew clean build
```

---

## Selesai! 🎉
## Selesai!
Database Supabase sudah siap digunakan. Sekarang Anda bisa:

1. Jalankan aplikasi DuitTracker
2. Register akun baru
3. Mulai mencatat transaksi

---

## Troubleshooting

### Error: "relation transactions does not exist"
- Pastikan SQL untuk membuat tabel sudah dijalankan dengan benar

### Error: "new row violates row-level security policy"
- Pastikan RLS policies sudah dibuat
- Pastikan user sudah login sebelum melakukan operasi

### Gambar tidak muncul
- Pastikan bucket `receipts` sudah dibuat
- Pastikan bucket bersifat **public**
- Pastikan storage policies sudah dikonfigurasi


### Error: "invalid input value for enum account_source"
- Pastikan nilai account_source menggunakan lowercase: `cash`, `bank`, `ewallet`

---

## Schema Reference

### Tabel transactions

| Column | Type | Nullable | Default |
|--------|------|----------|---------|
| id | UUID | NO | gen_random_uuid() |
| user_id | UUID | NO | - |
| amount | NUMERIC | NO | - |
| category | transaction_category | NO | - |
| type | transaction_type | NO | - |
| account_source | account_source | NO | 'cash' |
| note | TEXT | NO | '' |
| description | TEXT | YES | - |
| image_path | TEXT | YES | - |
| transaction_date | TIMESTAMPTZ | NO | NOW() |
| created_at | TIMESTAMPTZ | NO | NOW() |
| updated_at | TIMESTAMPTZ | NO | NOW() |

### Tabel profiles

| Column | Type | Nullable | Default |
|--------|------|----------|---------|
| id | UUID | NO | - |
| email | TEXT | NO | - |
| name | TEXT | NO | - |
| avatar_url | TEXT | YES | - |
| currency | TEXT | YES | 'IDR' |
| monthly_budget | NUMERIC | YES | - |
| created_at | TIMESTAMPTZ | YES | NOW() |
| updated_at | TIMESTAMPTZ | YES | NOW() |

### Enum Values

**transaction_category**: `food`, `transport`, `shopping`, `entertainment`, `bills`, `health`, `education`, `social`, `salary`, `investment`, `gift`, `daily_needs`, `other`

**transaction_type**: `income`, `expense`

**account_source**: `cash`, `bank`, `ewallet`