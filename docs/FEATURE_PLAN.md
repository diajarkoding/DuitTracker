 # DuitTracker - Feature Enhancement Plan
 
 ## Overview
 
 Dokumen ini berisi rencana implementasi untuk fitur-fitur baru DuitTracker:
 1. Halaman Profile dengan navigasi dari Dashboard
 2. Localization (ID/EN)
 3. Local Notification untuk reminder catatan keuangan
 4. Statistik/Laporan dengan filter periode
 5. Refactor Export Excel dengan multiple format
 
 ---
 
 ## 1. Halaman Profile
 
 ### 1.1 Perubahan UI Dashboard
 
 **File:** `ui/features/dashboard/DashboardScreen.kt`
 
 **Perubahan:**
 - Hapus button `Statistics` (BarChart icon) dan `Logout` dari header
 - Ganti dengan Circle Avatar yang clickable
 - Avatar menampilkan initial nama user atau gambar profile
 
 ```kotlin
 // Sebelum (2 button)
 Row(horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
     NeoIconButton(onClick = onStatsClick) { /* Stats icon */ }
     NeoIconButton(onClick = viewModel::logout) { /* Logout icon */ }
 }
 
 // Sesudah (1 avatar)
 NeoAvatar(
     userName = uiState.userName,
     avatarUrl = uiState.avatarUrl,
     onClick = onProfileClick
 )
 ```
 
 ### 1.2 Profile Screen Baru
 
 **Files baru:**
 - `ui/features/profile/ProfileScreen.kt`
 - `ui/features/profile/ProfileViewModel.kt`
 
 **Struktur UI Profile:**
 ```
 ┌─────────────────────────────────┐
 │  ← Profile                      │
 ├─────────────────────────────────┤
 │         [Avatar Circle]         │
 │          User Name              │
 │          user@email.com         │
 ├─────────────────────────────────┤
 │  📊 Statistics              →   │
 ├─────────────────────────────────┤
 │  🌐 Language                    │
 │     ○ Indonesia  ● English      │
 ├─────────────────────────────────┤
 │  🔔 Daily Reminder              │
 │     [Toggle Switch]             │
 │     Time: 20:00            →    │
 ├─────────────────────────────────┤
 │  🚪 Logout                      │
 └─────────────────────────────────┘
 ```
 
 **State:**
 ```kotlin
 data class ProfileUiState(
     val userName: String = "",
     val email: String = "",
     val avatarUrl: String? = null,
     val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
     val isReminderEnabled: Boolean = false,
     val reminderTime: LocalTime = LocalTime(20, 0),
     val isLoading: Boolean = false
 )
 
 enum class AppLanguage { INDONESIAN, ENGLISH }
 ```
 
 ### 1.3 Navigation Update
 
 **File:** `ui/navigation/Routes.kt`
 ```kotlin
 @Serializable
 data object Profile : Routes
 ```
 
 **File:** `ui/navigation/NavGraph.kt`
 - Tambah route ke Profile
 - Update DashboardScreen callback `onProfileClick`
 
 ### 1.4 Component Baru
 
 **File:** `ui/components/NeoAvatar.kt`
 ```kotlin
 @Composable
 fun NeoAvatar(
     userName: String,
     avatarUrl: String? = null,
     size: Dp = 40.dp,
     onClick: () -> Unit
 )
 ```
 
 ---
 
 ## 2. Localization (ID/EN)
 
 ### 2.1 String Resources
 
 **Files baru:**
 - `res/values/strings.xml` (English - default)
 - `res/values-id/strings.xml` (Indonesian)
 
 **Contoh strings.xml (EN):**
 ```xml
 <resources>
     <string name="app_name">DuitTracker</string>
     
     <!-- Dashboard -->
     <string name="greeting_morning">Good Morning</string>
     <string name="greeting_afternoon">Good Afternoon</string>
     <string name="greeting_evening">Good Evening</string>
     <string name="balance">Balance</string>
     <string name="income">Income</string>
     <string name="expense">Expense</string>
     <string name="no_transactions">No transactions this month</string>
     
     <!-- Profile -->
     <string name="profile">Profile</string>
     <string name="statistics">Statistics</string>
     <string name="language">Language</string>
     <string name="daily_reminder">Daily Reminder</string>
     <string name="reminder_time">Reminder Time</string>
     <string name="logout">Logout</string>
     
     <!-- Categories -->
     <string name="category_food">Food</string>
     <string name="category_transport">Transport</string>
     <!-- ... etc -->
 </resources>
 ```
 
 ### 2.2 Language Preference Storage
 
 **File:** `data/local/preferences/AppPreferences.kt`
 ```kotlin
 @Singleton
 class AppPreferences @Inject constructor(
     @ApplicationContext private val context: Context
 ) {
     companion object {
         private val LANGUAGE_KEY = stringPreferencesKey("app_language")
         private val REMINDER_ENABLED_KEY = booleanPreferencesKey("reminder_enabled")
         private val REMINDER_TIME_KEY = stringPreferencesKey("reminder_time")
     }
     
     val language: Flow<AppLanguage>
     suspend fun setLanguage(language: AppLanguage)
     
     val isReminderEnabled: Flow<Boolean>
     suspend fun setReminderEnabled(enabled: Boolean)
     
     val reminderTime: Flow<LocalTime>
     suspend fun setReminderTime(time: LocalTime)
 }
 ```
 
 ### 2.3 Locale Configuration
 
 **File:** `DuitTrackerApp.kt` atau `MainActivity.kt`
 ```kotlin
 // Observe language preference dan update locale
 private fun updateLocale(language: AppLanguage) {
     val locale = when (language) {
         AppLanguage.INDONESIAN -> Locale("id", "ID")
         AppLanguage.ENGLISH -> Locale.ENGLISH
     }
     val config = Configuration(resources.configuration)
     config.setLocale(locale)
     resources.updateConfiguration(config, resources.displayMetrics)
 }
 ```
 
 ### 2.4 Update Existing Code
 
 **Files yang perlu di-update untuk menggunakan stringResource():**
 - `DashboardScreen.kt` - greeting, labels
 - `StatisticsScreen.kt` - headers, labels
 - `AddTransactionScreen.kt` - form labels
 - `EditTransactionScreen.kt` - form labels
 - `TransactionDetailScreen.kt` - labels
 - `CategoryUtils.kt` - category display names
 - `ExcelExportRepository.kt` - report headers (opsional, bisa tetap ID)
 
 ---
 
 ## 3. Local Notification (Daily Reminder)
 
 ### 3.1 Dependencies
 
 **File:** `app/build.gradle.kts`
 ```kotlin
 // Sudah ada WorkManager, tidak perlu tambahan
 // Notification permission untuk Android 13+
 ```
 
 ### 3.2 Notification Helper
 
 **File baru:** `data/notification/ReminderNotificationManager.kt`
 ```kotlin
 @Singleton
 class ReminderNotificationManager @Inject constructor(
     @ApplicationContext private val context: Context
 ) {
     companion object {
         const val CHANNEL_ID = "daily_reminder"
         const val NOTIFICATION_ID = 1001
     }
     
     fun createNotificationChannel()
     
     fun showReminderNotification()
     
     fun scheduleReminder(time: LocalTime)
     
     fun cancelReminder()
 }
 ```
 
 ### 3.3 Reminder Worker
 
 **File baru:** `data/notification/ReminderWorker.kt`
 ```kotlin
 @HiltWorker
 class ReminderWorker @AssistedInject constructor(
     @Assisted context: Context,
     @Assisted params: WorkerParameters,
     private val notificationManager: ReminderNotificationManager
 ) : CoroutineWorker(context, params) {
     
     override suspend fun doWork(): Result {
         notificationManager.showReminderNotification()
         return Result.success()
     }
 }
 ```
 
 ### 3.4 Notification Content
 
 ```kotlin
 // Notification akan menampilkan:
 // Title: "DuitTracker Reminder"
 // Body: "Don't forget to record your expenses today! 📝"
 // atau dalam ID: "Jangan lupa catat pengeluaran hari ini! 📝"
 // 
 // Action: Klik untuk buka app ke Dashboard
 ```
 
 ### 3.5 Permission Handling
 
 **File:** `ui/features/profile/ProfileScreen.kt`
 ```kotlin
 // Request POST_NOTIFICATIONS permission untuk Android 13+
 val notificationPermissionLauncher = rememberLauncherForActivityResult(
     ActivityResultContracts.RequestPermission()
 ) { granted ->
     if (granted) {
         viewModel.enableReminder()
     }
 }
 ```
 
 ### 3.6 Time Picker Dialog
 
 **File baru:** `ui/components/NeoTimePicker.kt`
 ```kotlin
 @Composable
 fun NeoTimePickerDialog(
     selectedTime: LocalTime,
     onTimeSelected: (LocalTime) -> Unit,
     onDismiss: () -> Unit
 )
 ```
 
 ---
 
 ## 4. Statistik/Laporan dengan Filter Periode
 
 ### 4.1 Update StatisticsScreen UI
 
 **File:** `ui/features/statistics/StatisticsScreen.kt`
 
 **Perubahan:**
 - Tambah Period Selector (All, Monthly, Weekly)
 - Update tampilan berdasarkan periode yang dipilih
 
 ```
 ┌─────────────────────────────────┐
 │  ← Statistics          [Export]│
 ├─────────────────────────────────┤
 │  [All] [Monthly] [Weekly]      │  <- Period Selector
 ├─────────────────────────────────┤
 │  ← December 2024 →             │  <- Month/Week Navigator
 ├─────────────────────────────────┤
 │  [Expense Card] [Income Card]  │
 ├─────────────────────────────────┤
 │  Expense by Category           │
 │  [Pie Chart]                   │
 │  [Category List...]            │
 ├─────────────────────────────────┤
 │  Income by Category            │
 │  [Pie Chart]                   │
 │  [Category List...]            │
 └─────────────────────────────────┘
 ```
 
 ### 4.2 Update StatisticsViewModel
 
 **File:** `ui/features/statistics/StatisticsViewModel.kt`
 
 **Perubahan State:**
 ```kotlin
 enum class StatisticsPeriod { ALL, MONTHLY, WEEKLY }
 
 data class StatisticsUiState(
     // Existing fields...
     val selectedPeriod: StatisticsPeriod = StatisticsPeriod.MONTHLY,
     
     // For WEEKLY mode
     val availableWeeks: List<WeekData> = emptyList(),
     val selectedWeekIndex: Int = 0,
     val selectedWeekName: String = "",
     
     // For ALL mode - aggregate semua data
     val allTimeData: AllTimeStatistics? = null
 )
 
 data class WeekData(
     val startDate: LocalDate,
     val endDate: LocalDate,
     val weekNumber: Int,
     val year: Int
 )
 
 data class AllTimeStatistics(
     val totalIncome: Double,
     val totalExpense: Double,
     val transactionCount: Int,
     val firstTransactionDate: LocalDate?,
     val lastTransactionDate: LocalDate?
 )
 ```
 
 **Functions baru:**
 ```kotlin
 fun setPeriod(period: StatisticsPeriod)
 fun previousWeek()
 fun nextWeek()
 private fun loadAllTimeStatistics()
 private fun loadWeeklyStatistics()
 ```
 
 ### 4.3 Weekly Statistics Logic
 
 ```kotlin
 // Week calculation menggunakan ISO week (Monday = first day)
 fun getWeekRange(date: LocalDate): Pair<LocalDate, LocalDate> {
     val startOfWeek = date.minus(date.dayOfWeek.ordinal, DateTimeUnit.DAY)
     val endOfWeek = startOfWeek.plus(6, DateTimeUnit.DAY)
     return Pair(startOfWeek, endOfWeek)
 }
 ```
 
 ---
 
 ## 5. Refactor Export Excel
 
 ### 5.1 Export Options
 
 **File:** `ui/features/statistics/StatisticsScreen.kt`
 
 Tambah dialog untuk memilih format export:
 
 ```
 ┌─────────────────────────────────┐
 │  Export Report                  │
 ├─────────────────────────────────┤
 │  ○ All Data (Complete)         │
 │  ○ Monthly Report              │
 │  ○ Weekly Report               │
 │  ○ By Category                 │
 ├─────────────────────────────────┤
 │  [Cancel]          [Export]    │
 └─────────────────────────────────┘
 ```
 
 ### 5.2 Update ExcelExportRepository
 
 **File:** `data/repository/ExcelExportRepository.kt`
 
 **Functions baru:**
 ```kotlin
 enum class ExportType {
     ALL_DATA,      // Semua transaksi dari awal
     MONTHLY,       // Transaksi bulan tertentu
     WEEKLY,        // Transaksi minggu tertentu
     BY_CATEGORY    // Grouped by category dengan detail
 }
 
 suspend fun exportAllData(
     transactions: List<Transaction>
 ): Result<Uri>
 
 suspend fun exportMonthly(
     transactions: List<Transaction>,
     monthName: String,
     // existing params...
 ): Result<Uri>  // Existing function, renamed
 
 suspend fun exportWeekly(
     transactions: List<Transaction>,
     weekStart: LocalDate,
     weekEnd: LocalDate
 ): Result<Uri>
 
 suspend fun exportByCategory(
     transactions: List<Transaction>,
     category: TransactionCategory
 ): Result<Uri>
 ```
 
 ### 5.3 Sheet Structure per Export Type
 
 **ALL_DATA Export:**
 - Sheet 1: Summary (All Time)
 - Sheet 2: Monthly Breakdown (per bulan)
 - Sheet 3: Category Summary
 - Sheet 4: All Transactions
 
 **MONTHLY Export:** (existing, tetap sama)
 - Sheet 1: Summary
 - Sheet 2: Category Details
 - Sheet 3: Daily Details
 - Sheet 4: Income
 - Sheet 5: Expense
 - Sheet 6: All Transactions
 
 **WEEKLY Export:**
 - Sheet 1: Weekly Summary
 - Sheet 2: Daily Breakdown (7 hari)
 - Sheet 3: Category Summary
 - Sheet 4: All Transactions
 
 **BY_CATEGORY Export:**
 - Sheet 1: Category Summary
 - Sheet 2: Transaction List (filtered by category)
 - Sheet 3: Daily Trend
 
 ---
 
 ## Implementation Order
 
 ### Phase 1: Foundation (1-2 hari)
 1. [ ] Buat `AppPreferences.kt` untuk menyimpan settings
 2. [ ] Buat string resources (`strings.xml`, `strings-id.xml`)
 3. [ ] Buat `NeoAvatar.kt` component
 
 ### Phase 2: Profile Screen (1-2 hari)
 4. [ ] Buat `ProfileScreen.kt` dan `ProfileViewModel.kt`
 5. [ ] Update Routes dan NavGraph
 6. [ ] Update DashboardScreen - ganti buttons dengan avatar
 7. [ ] Implementasi language switching
 
 ### Phase 3: Notification (1-2 hari)
 8. [ ] Buat `ReminderNotificationManager.kt`
 9. [ ] Buat `ReminderWorker.kt`
 10. [ ] Buat `NeoTimePickerDialog.kt`
 11. [ ] Integrasi dengan ProfileScreen
 12. [ ] Handle notification permission
 
 ### Phase 4: Statistics Enhancement (1-2 hari)
 13. [ ] Update `StatisticsViewModel.kt` - tambah period logic
 14. [ ] Update `StatisticsScreen.kt` - tambah period selector
 15. [ ] Implementasi weekly statistics calculation
 16. [ ] Implementasi all-time statistics
 
 ### Phase 5: Excel Export Refactor (1-2 hari)
 17. [ ] Buat export type selection dialog
 18. [ ] Implementasi `exportAllData()`
 19. [ ] Implementasi `exportWeekly()`
 20. [ ] Implementasi `exportByCategory()`
 21. [ ] Update StatisticsViewModel untuk handle export options
 
 ### Phase 6: Localization Completion (1 hari)
 22. [ ] Update semua screens untuk menggunakan stringResource()
 23. [ ] Update CategoryUtils untuk localized names
 24. [ ] Testing kedua bahasa
 
 ---
 
 ## File Structure After Implementation
 
 ```
 app/src/main/java/com/diajarkoding/duittracker/
 ├── data/
 │   ├── local/
 │   │   └── preferences/
 │   │       ├── SyncPreferences.kt (existing)
 │   │       └── AppPreferences.kt (NEW)
 │   ├── notification/
 │   │   ├── ReminderNotificationManager.kt (NEW)
 │   │   └── ReminderWorker.kt (NEW)
 │   └── repository/
 │       └── ExcelExportRepository.kt (UPDATED)
 │
 ├── ui/
 │   ├── components/
 │   │   ├── NeoAvatar.kt (NEW)
 │   │   └── NeoTimePickerDialog.kt (NEW)
 │   ├── features/
 │   │   ├── dashboard/
 │   │   │   └── DashboardScreen.kt (UPDATED)
 │   │   ├── profile/
 │   │   │   ├── ProfileScreen.kt (NEW)
 │   │   │   └── ProfileViewModel.kt (NEW)
 │   │   └── statistics/
 │   │       ├── StatisticsScreen.kt (UPDATED)
 │   │       └── StatisticsViewModel.kt (UPDATED)
 │   └── navigation/
 │       ├── Routes.kt (UPDATED)
 │       └── NavGraph.kt (UPDATED)
 │
 └── res/
     ├── values/
     │   └── strings.xml (NEW/UPDATED)
     └── values-id/
         └── strings.xml (NEW)
 ```
 
 ---
 
 ## Testing Checklist
 
 ### Profile Screen
 - [ ] Avatar clickable dan navigasi ke Profile
 - [ ] Data profile ditampilkan dengan benar
 - [ ] Navigate ke Statistics dari Profile
 - [ ] Logout berfungsi
 
 ### Localization
 - [ ] Switch bahasa langsung apply tanpa restart
 - [ ] Semua text berubah sesuai bahasa
 - [ ] Preference tersimpan dan persist
 
 ### Notification
 - [ ] Permission request untuk Android 13+
 - [ ] Time picker berfungsi
 - [ ] Notification muncul pada waktu yang ditentukan
 - [ ] Klik notification membuka app
 - [ ] Toggle on/off berfungsi
 
 ### Statistics
 - [ ] Period selector (All/Monthly/Weekly) berfungsi
 - [ ] Data ditampilkan sesuai period
 - [ ] Navigation week/month berfungsi
 - [ ] All-time aggregation benar
 
 ### Excel Export
 - [ ] Dialog export options muncul
 - [ ] Export All Data menghasilkan file yang benar
 - [ ] Export Weekly menghasilkan file yang benar
 - [ ] Export By Category menghasilkan file yang benar
 - [ ] Share intent berfungsi
