# Sistem Pendaftaran & Manajemen Antrian Pemeriksaan Pasien
## Nasihuy Hospital Management System

---

## 🎯 Fitur Utama

### 1. **Pendaftaran Pasien Cepat (Quick Patient Registration)**
**File:** `QuickPatientRegistrationView.java`

#### Alur Pendaftaran:
```
Login Page
    ↓
"Daftar Sebagai Pasien Periksa" Button
    ↓
Quick Registration Form (nama, telepon, email, alamat, keluhan)
    ↓
Validasi Data
    ↓
Generate Patient Number (APXXX format)
    ↓
Calculate Estimated Wait Time (5 menit per pasien sebelumnya)
    ↓
Insert ke Database
    ↓
Send Confirmation Email
    ↓
Display Patient Number & Wait Time
    ↓
Return to Login
```

#### Form Input (Wajib):
- **Nama Pasien** *(minimal 3 karakter)*
- **No. Telepon** *(10-13 digit)*
- **Email** *(format valid, WAJIB)*
- **Alamat** *(minimal 5 karakter)*
- **Keluhan/Gejala** *(minimal 3 karakter)*

#### Proses Otomatis:
1. **Generate Nomor Pasien:**
   - Format: `APXXX` (AP001, AP002, AP003, dst)
   - Query: `SELECT MAX(patient_number) FROM queues`
   - Ensure unique per pasien

2. **Hitung Perkiraan Waktu Tunggu:**
   - Query: `SELECT COUNT(*) FROM queues WHERE status = 'Menunggu'`
   - Rumus: `pending_count × 5 menit`
   - Contoh: Jika ada 3 pasien menunggu = 15 menit

3. **Kirim Email Konfirmasi:**
   - Penerima: Email pasien yang terdaftar
   - Isi: Nomor pasien, perkiraan waktu tunggu, waktu registrasi
   - Format: HTML dengan branding Nasihuy Hospital
   - Service: `AccountService.sendRegistrationConfirmation()`

4. **Insert ke Database:**
   ```sql
   INSERT INTO queues (
       patient_number,    -- APXXX
       patient_name,      -- Dari input
       phone,             -- Dari input
       email,             -- Dari input
       address,           -- Dari input
       symptoms,          -- Dari input
       status,            -- 'Menunggu'
       queue_number,      -- Auto-increment per hari
       registration_time  -- NOW()
   ) VALUES (...)
   ```

---

### 2. **Manajemen Antrian (Queue Management)**
**File:** `QueueManagementView.java`

#### Dashboard Fitur:
```
┌─────────────────────────────────────────┐
│      MANAJEMEN ANTRIAN PEMERIKSAAN      │
├─────────────────────────────────────────┤
│ 📊 Total Pasien: X | ⏳ Menunggu: Y | ✓ Selesai: Z │
├─────────────────────────────────────────┤
│ Tabel Antrian:                          │
│ ┌──────┬───┬───────────┬────────┬─────┐ │
│ │No.AP │No.│Nama Pasien│Telepon │Aksi │ │
│ ├──────┼───┼───────────┼────────┼─────┤ │
│ │AP001 │1  │Budi Santoso│08123..│[▶] │ │
│ │AP002 │2  │Siti Rahma  │08124..│[▶] │ │
│ └──────┴───┴───────────┴────────┴─────┘ │
└─────────────────────────────────────────┘
```

#### Fungsi Status Pasien:
| Status | Deskripsi | Aksi |
|--------|-----------|------|
| **Menunggu** | Baru terdaftar, belum diperiksa | Tombol "Mulai Periksa" |
| **Sedang Diperiksa** | Sedang dalam proses pemeriksaan | Tombol "Selesai" |
| **Selesai** | Pemeriksaan selesai, keluar antrian | *(Tidak ada aksi)* |

#### Statistik Real-time:
- **Total Pasien Hari Ini:** Jumlah seluruh pendaftar hari ini
- **Menunggu:** Pasien dengan status "Menunggu"
- **Selesai:** Pasien dengan status "Selesai"

#### Auto-Refresh:
- Data refresh setiap **5 detik** otomatis
- Tidak perlu manual klik refresh
- Update status real-time

#### Kolom Tabel:
1. **Nomor Pasien** (APXXX)
2. **No. Antrian** (1, 2, 3, ...)
3. **Nama Pasien**
4. **Telepon**
5. **Keluhan**
6. **Status** (dengan color coding)
7. **Aksi** (Tombol sesuai status)

---

## 📊 Database Schema Updates

### Tabel: `queues`

```sql
ALTER TABLE queues ADD COLUMN (
    patient_number VARCHAR(20) UNIQUE,    -- APXXX format
    phone VARCHAR(20),                     -- Nomor telepon pasien
    email VARCHAR(100),                    -- Email pasien
    address VARCHAR(255),                  -- Alamat lengkap
    symptoms VARCHAR(255),                 -- Keluhan/gejala
    registration_time DATETIME            -- Waktu pendaftaran
);
```

#### Field Penting:
- `patient_number` → Unik per pasien (AP001, AP002, dst)
- `queue_number` → Urutan antrian per hari (1, 2, 3, dst)
- `status` → Menunggu | Sedang Diperiksa | Selesai
- `registration_time` → Timestamp otomatis

---

## 📧 Email Notification

### Service: `AccountService.sendRegistrationConfirmation()`

#### Contoh Email Template:

```
Subject: Konfirmasi Pendaftaran - Nasihuy Hospital

───────────────────────────────────────
Selamat Datang di Nasihuy Hospital

Halo Budi Santoso,

Terima kasih telah melakukan pendaftaran. Berikut detail 
pendaftaran Anda:

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Nomor Pasien: AP001
Perkiraan Waktu Tunggu: 15 menit
Waktu Pendaftaran: 01/12/2025 14:30:45
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Silahkan datang ke rumah sakit dan tunjukkan nomor pasien 
Anda kepada petugas di bagian pendaftaran.

© 2025 Nasihuy Hospital All rights reserved.
───────────────────────────────────────
```

### Konfigurasi Email:
```bash
# .env atau Environment Variables
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=nasihuyhospital@gmail.com
SMTP_PASS=[16-char App Password dari Google]
```

---

## 🔄 Alur Kerja Lengkap

### Skenario: Pasien Baru Mendaftar

```
1. PASIEN AKSES LOGIN PAGE
   ├─ Lihat button "Daftar Sebagai Pasien Periksa"
   └─ Klik button → Masuk Quick Registration View

2. ISI FORM REGISTRASI
   ├─ Nama: "Budi Santoso"
   ├─ Telepon: "081234567890"
   ├─ Email: "budi@email.com"
   ├─ Alamat: "Jl. Merdeka No. 123, Jakarta"
   └─ Keluhan: "Demam tinggi dan sakit kepala"

3. VALIDASI DATA
   ├─ Nama ✓ (≥3 karakter)
   ├─ Telepon ✓ (10-13 digit)
   ├─ Email ✓ (format valid)
   ├─ Alamat ✓ (≥5 karakter)
   └─ Keluhan ✓ (≥3 karakter)

4. GENERATE NOMOR PASIEN
   ├─ Query: SELECT MAX(SUBSTRING(patient_number, 3))
   ├─ Hasil: Nomor sebelumnya adalah AP015
   └─ Generate: AP016

5. HITUNG WAKTU TUNGGU
   ├─ Query: SELECT COUNT(*) WHERE status='Menunggu'
   ├─ Hasil: 3 pasien menunggu
   └─ Kalkulasi: 3 × 5 menit = 15 menit

6. INSERT KE DATABASE
   ├─ Table: queues
   ├─ patient_number: AP016
   ├─ patient_name: Budi Santoso
   ├─ phone: 081234567890
   ├─ email: budi@email.com
   ├─ address: Jl. Merdeka No. 123, Jakarta
   ├─ symptoms: Demam tinggi dan sakit kepala
   ├─ status: Menunggu
   ├─ queue_number: 5 (urutan hari ini)
   └─ registration_time: 2025-12-01 14:30:45

7. KIRIM EMAIL KONFIRMASI
   ├─ To: budi@email.com
   ├─ Subject: Konfirmasi Pendaftaran - Nasihuy Hospital
   ├─ Body: HTML email dengan info pasien
   └─ Status: ✓ Terkirim

8. TAMPILKAN NOTIFIKASI SUKSES
   ├─ Nomor Pasien: AP016
   ├─ Waktu Tunggu: 15 menit
   └─ Email telah dikirim ke: budi@email.com

9. KEMBALI KE LOGIN PAGE
   └─ Pasien siap untuk pemeriksaan
```

### Skenario: Admin Kelola Antrian

```
1. ADMIN LOGIN & AKSES QUEUE MANAGEMENT
   ├─ Dashboard → Tombol "Kelola Antrian"
   └─ Masuk Queue Management View

2. LIHAT STATISTIK
   ├─ Total Pasien Hari Ini: 5
   ├─ Menunggu: 3
   └─ Selesai: 2

3. LIHAT DAFTAR ANTRIAN
   ├─ AP001 | No.1 | Budi Santoso | 081234... | ⏳ Menunggu | [Mulai Periksa]
   ├─ AP002 | No.2 | Siti Rahma   | 081235... | ⏳ Menunggu | [Mulai Periksa]
   └─ AP003 | No.3 | Ahmad Riko   | 081236... | 🔄 Sedang Diperiksa | [Selesai]

4. KLIK "MULAI PERIKSA" UNTUK AP001
   ├─ Update status: Menunggu → Sedang Diperiksa
   ├─ Tombol berubah menjadi: [Selesai]
   └─ Database: UPDATE queues SET status='Sedang Diperiksa' WHERE patient_number='AP001'

5. KLIK "SELESAI" SETELAH PEMERIKSAAN
   ├─ Update status: Sedang Diperiksa → Selesai
   ├─ Pasien keluar dari antrian
   ├─ Statistik "Selesai" bertambah 1
   └─ Database: UPDATE queues SET status='Selesai' WHERE patient_number='AP001'

6. AUTO-REFRESH SETIAP 5 DETIK
   └─ Data selalu updated tanpa manual refresh
```

---

## 🛠️ Implementasi Teknis

### Class: `QuickPatientRegistrationView`

**Method Utama:**
```java
handleRegistration()              // Validasi dan process
generatePatientNumber()          // Generate APXXX
calculateEstimatedWaitTime()    // Hitung 5 min/pasien
insertPatientToQueue()           // Insert ke DB
sendRegistrationEmail()          // Kirim email konfirmasi
```

### Class: `QueueManagementView`

**Method Utama:**
```java
refreshData()                    // Fetch & display queue
updateQueueStatus()              // Update status pasien
startAutoRefresh()              // Auto-refresh setiap 5 detik
createStatsPanel()              // Buat statistik
createTablePanel()              // Buat tabel antrian
```

### Database Updates: `DBConnection.java`

**Method:**
```java
ensureQueueExtraColumnsExists()  // Tambah kolom baru ke tabel queues
```

---

## 🎨 UI Components

### Login Page Enhancement:
```
┌────────────────────────────────────────┐
│                                        │
│    [← Sign In Button]   [Hospital]     │
│                                        │
│    Username/Email: [_____________]    │
│    Password:       [_____________]    │
│                                        │
│    ☑ Remember Me        Forgot?        │
│                                        │
│    [════════ Sign In ════════]         │
│                                        │
│    Don't have account? Register        │
│                                        │
│    [== Daftar Sbg Pasien Periksa ==]  │ ← NEW BUTTON
│                                        │
│    © 2025 Nasihuy Hospital             │
│                                        │
└────────────────────────────────────────┘
```

### Queue Management Dashboard:
```
┌──────────────────────────────────────┐
│ [← Back] [📋 Kelola Antrian] Title   │
├──────────────────────────────────────┤
│ 📊 Total: 5 | ⏳ Menunggu: 3 | ✓ Selesai: 2 │
├──────────────────────────────────────┤
│ Daftar Antrian:                      │
│ ┌─────────────────────────────────┐  │
│ │ APXXX │ No │ Nama │ Telepon │ ✓│  │
│ ├─────────────────────────────────┤  │
│ │ AP001 │ 1  │ Budi │ 08123.. │[▶]  │
│ │ AP002 │ 2  │ Siti │ 08124.. │[▶]  │
│ │ AP003 │ 3  │ Ahmad│ 08125.. │[✓]  │
│ └─────────────────────────────────┘  │
│               [🔄 Refresh]           │
└──────────────────────────────────────┘
```

---

## ✅ Testing Checklist

- [ ] Registrasi pasien dengan data lengkap
- [ ] Validasi email format
- [ ] Generate nomor pasien APXXX unik
- [ ] Hitung waktu tunggu 5 menit/pasien
- [ ] Email konfirmasi terkirim
- [ ] Data masuk tabel queues
- [ ] Admin bisa lihat daftar antrian
- [ ] Update status Menunggu → Sedang Diperiksa
- [ ] Update status Sedang Diperiksa → Selesai
- [ ] Auto-refresh setiap 5 detik
- [ ] Statistik ter-update real-time

---

## 🚀 Deployment Notes

1. **Email Configuration:**
   ```bash
   Set Environment Variables:
   - SMTP_HOST=smtp.gmail.com
   - SMTP_PORT=587
   - SMTP_USER=nasihuyhospital@gmail.com
   - SMTP_PASS=[16-char App Password]
   ```

2. **Database Preparation:**
   ```sql
   -- Sudah otomatis via DBConnection.createTables()
   ALTER TABLE queues ADD COLUMNS (
       patient_number, phone, email, address, 
       symptoms, registration_time
   );
   ```

3. **Restart Application:**
   - Database schema akan auto-update
   - Email service akan siap
   - Queue Management ready to use

---

## 📱 User Roles

| Role | Access | Fungsi |
|------|--------|---------|
| **Pasien** | Quick Registration | Daftar & lihat nomor pasien |
| **Admin/Staff** | Queue Management | Monitor & kelola antrian |

---

**Dibuat untuk:** Nasihuy Hospital Management System
**Fitur:** Quick Patient Registration + Queue Management
**Last Updated:** 01 Desember 2025
