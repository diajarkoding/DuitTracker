# Arsitektur Aplikasi

Dokumen ini menjelaskan arsitektur dan pola desain yang digunakan dalam pengembangan aplikasi DuitTracker.

---

## Daftar Isi

1. [Clean Architecture](#clean-architecture)
2. [MVVM Pattern](#mvvm-pattern)
3. [Remote-First dengan Offline Fallback](#remote-first-dengan-offline-fallback)
4. [Dependency Injection](#dependency-injection)
5. [Struktur Folder](#struktur-folder)

---

## Clean Architecture

DuitTracker mengikuti prinsip Clean Architecture yang membagi kode menjadi beberapa layer terpisah:

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer                              │
│              (Compose + ViewModel)                       │
│    - Menampilkan data ke pengguna                       │
│    - Menangani interaksi pengguna                       │
└─────────────────────────┬───────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                  Domain Layer                            │
│         (Repository Interfaces + Models)                 │
│    - Mendefinisikan kontrak bisnis                      │
│    - Tidak bergantung pada implementasi                 │
└─────────────────────────┬───────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                   Data Layer                             │
│     (Repository Impl + Database + Network)              │
│    - Implementasi akses data                            │
│    - Mengelola cache dan sync                           │
└─────────────────────────────────────────────────────────┘
```

### Keuntungan

- **Testability**: Setiap layer bisa ditest secara terpisah
- **Flexibility**: Mudah mengganti implementasi (misal: ganti database)
- **Maintainability**: Perubahan di satu layer tidak mempengaruhi layer lain

---

## MVVM Pattern

Aplikasi menggunakan pola Model-View-ViewModel untuk layer presentasi:

```
┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
│       View       │      │    ViewModel     │      │    Repository    │
│   (Composable)   │◄────►│   (StateFlow)    │◄────►│  (Data Source)   │
└──────────────────┘      └──────────────────┘      └──────────────────┘
         │                         │
         │  Observes State         │  Calls Methods
         │                         │
         ▼                         ▼
    UI Updates              Business Logic
```

### Komponen MVVM

**1. View (Composable Functions)**
- Menampilkan UI berdasarkan state
- Meneruskan aksi pengguna ke ViewModel
- Tidak menyimpan business logic

```kotlin
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Render UI berdasarkan state
    if (uiState.isLoading) {
        LoadingIndicator()
    } else {
        TransactionList(uiState.transactions)
    }
}
```

**2. ViewModel**
- Menyimpan dan mengelola UI state
- Menangani business logic
- Berkomunikasi dengan Repository

```kotlin
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: ITransactionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    fun loadTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // ... fetch data
        }
    }
}
```

**3. Model (Data Classes)**
- Representasi data dalam aplikasi
- Immutable untuk keamanan thread

```kotlin
data class Transaction(
    val id: String,
    val amount: Double,
    val category: TransactionCategory,
    // ...
)
```

---

## Remote-First dengan Offline Fallback

Arsitektur data mengutamakan server sebagai sumber kebenaran, dengan fallback ke cache lokal saat offline:

```
┌─────────────────────────────────────────────────────────┐
│                   NetworkMonitor                         │
│           (Memantau status koneksi internet)             │
└─────────────────────────┬───────────────────────────────┘
                          │
         ┌────────────────┴────────────────┐
         │                                 │
    ┌────▼────┐                       ┌────▼────┐
    │ ONLINE  │                       │ OFFLINE │
    └────┬────┘                       └────┬────┘
         │                                 │
         ▼                                 ▼
┌─────────────────┐               ┌─────────────────┐
│    Supabase     │               │      Room       │
│   (Primary)     │               │    (Cache)      │
│                 │               │                 │
│ - Auth          │               │ - Transactions  │
│ - Database      │               │ - Pending Ops   │
│ - Storage       │               │                 │
└────────┬────────┘               └─────────────────┘
         │
         │  Cache
         ▼
┌─────────────────┐
│      Room       │
│   (Local DB)    │
└─────────────────┘
```

### Alur Operasi CRUD

**Saat Online:**
1. Operasi langsung ke Supabase
2. Hasil di-cache ke Room
3. UI di-update dengan data terbaru

**Saat Offline:**
1. Operasi disimpan ke Room
2. Ditambahkan ke Pending Queue
3. Saat online, auto-sync ke server

### Keuntungan Pendekatan Ini

| Aspek | Remote-First | Offline-First (Sebelumnya) |
|-------|--------------|---------------------------|
| Data Freshness | Selalu terbaru | Mungkin stale |
| Konflik | Minimal | Perlu resolusi |
| Complexity | Lebih sederhana | Lebih kompleks |
| Offline UX | Terbatas | Penuh |

---

## Dependency Injection

Menggunakan **Hilt** untuk dependency injection:

```
┌─────────────────────────────────────────────────────────┐
│                    Application                           │
│                   @HiltAndroidApp                        │
└─────────────────────────────────────────────────────────┘
                          │
          ┌───────────────┼───────────────┐
          ▼               ▼               ▼
    ┌───────────┐   ┌───────────┐   ┌───────────┐
    │ Database  │   │ Supabase  │   │  Network  │
    │  Module   │   │  Module   │   │  Module   │
    └─────┬─────┘   └─────┬─────┘   └─────┬─────┘
          │               │               │
          │    Provides   │    Provides   │
          ▼               ▼               ▼
    ┌───────────┐   ┌───────────┐   ┌───────────┐
    │   DAOs    │   │ Supabase  │   │  Network  │
    │ Database  │   │  Client   │   │  Monitor  │
    └───────────┘   └───────────┘   └───────────┘
```

### Modul yang Tersedia

**DatabaseModule** - Menyediakan Room Database dan DAOs
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DuitTrackerDatabase
    
    @Provides
    fun provideTransactionDao(db: DuitTrackerDatabase): TransactionDao
}
```

**SupabaseModule** - Menyediakan Supabase Client
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {
    @Provides @Singleton
    fun provideSupabaseClient(): SupabaseClient
}
```

**NetworkModule** - Menyediakan Network Monitor
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor
}
```

---

## Struktur Folder

```
app/src/main/java/com/diajarkoding/duittracker/
│
├── DuitTrackerApp.kt          # Application class
├── MainActivity.kt            # Entry point Activity
│
├── data/                      # Data Layer
│   ├── local/                 # Local Storage
│   │   ├── dao/               # Data Access Objects
│   │   ├── entity/            # Room Entities
│   │   ├── preferences/       # DataStore Preferences
│   │   └── DuitTrackerDatabase.kt
│   │
│   ├── remote/                # Remote Data
│   │   └── dto/               # Data Transfer Objects
│   │
│   ├── mapper/                # Entity ↔ DTO Mappers
│   ├── model/                 # Domain Models
│   ├── network/               # Network Utilities
│   ├── repository/            # Repository Implementations
│   └── sync/                  # Sync Manager & Worker
│
├── domain/                    # Domain Layer
│   ├── model/                 # Business Models
│   └── repository/            # Repository Interfaces
│
├── di/                        # Dependency Injection
│   ├── DatabaseModule.kt
│   ├── SupabaseModule.kt
│   ├── NetworkModule.kt
│   └── DataModule.kt
│
├── ui/                        # Presentation Layer
│   ├── components/            # Reusable UI Components
│   ├── features/              # Feature Modules
│   │   ├── splash/
│   │   ├── auth/
│   │   ├── dashboard/
│   │   ├── detail/
│   │   ├── input/
│   │   ├── edit/
│   │   └── statistics/
│   ├── navigation/            # Navigation Setup
│   └── theme/                 # Theme & Styling
│
└── utils/                     # Utility Classes
    ├── CategoryUtils.kt
    ├── CurrencyFormatter.kt
    └── DateFormatter.kt
```

### Penjelasan Folder

| Folder | Fungsi |
|--------|--------|
| `data/local` | Semua yang berkaitan dengan penyimpanan lokal |
| `data/remote` | DTO untuk komunikasi dengan Supabase |
| `data/repository` | Implementasi repository (gabungan local + remote) |
| `domain` | Interface dan model yang tidak bergantung implementasi |
| `di` | Konfigurasi Hilt untuk dependency injection |
| `ui/components` | Komponen UI yang bisa digunakan ulang |
| `ui/features` | Screen dan ViewModel per fitur |
| `utils` | Helper functions dan utilities |
