# DuitTracker - New Features Implementation Plan

## Overview

Dokumen ini berisi analisis dan planning komprehensif untuk dua fitur yang saat ini di-hide:
1. **Localization (Multi-Language Support)**
2. **Daily Reminder Notification**

---

## 1. LOCALIZATION FEATURE

### Current Implementation Status: ~85% Complete

#### ✅ Already Implemented:

| Component | Status | Description |
|-----------|--------|-------------|
| AppPreferences | ✅ Done | DataStore untuk menyimpan preferensi bahasa |
| AppLanguage Enum | ✅ Done | English dan Indonesian sudah didefinisikan |
| strings.xml (English) | ✅ Done | 100+ string resources untuk English |
| strings.xml (Indonesian) | ✅ Done | 100+ string resources untuk Indonesian (values-id) |
| ProfileViewModel | ✅ Done | Logic untuk set/get language preference |
| ProfileScreen UI | ✅ Done | UI language picker (saat ini di-hide) |

#### ❌ Missing/Incomplete:

| Component | Status | Description |
|-----------|--------|-------------|
| Activity Recreation | ❌ Missing | Belum ada logic untuk recreate Activity saat bahasa berubah |
| BaseApplication | ❌ Missing | Belum ada setup untuk apply locale di Application level |
| Locale Configuration | ❌ Missing | Belum ada wrapper untuk apply Configuration dengan locale baru |
| String Usage in Compose | ⚠️ Partial | Banyak screen masih menggunakan hardcoded string, bukan stringResource() |
| Dynamic Locale Change | ❌ Missing | Runtime language switching belum berfungsi |

### Implementation Plan:

#### Phase 1: Core Locale Infrastructure (Priority: HIGH)

**Task 1.1: Create LocaleHelper utility class**
```kotlin
// File: utils/LocaleHelper.kt
object LocaleHelper {
    fun setLocale(context: Context, language: AppLanguage): Context
    fun getLocale(context: Context): Locale
    fun wrapContext(context: Context): Context
}
```

**Task 1.2: Update Application class**
- Override `attachBaseContext()` untuk apply saved locale
- Load language preference saat app startup

**Task 1.3: Update MainActivity**
- Override `attachBaseContext()` untuk wrap context dengan locale
- Implement `recreate()` saat language berubah
- Handle configuration changes properly

**Task 1.4: Create LocaleChangeHandler**
- BroadcastReceiver atau callback untuk notify language change
- Trigger Activity recreation dengan smooth transition

#### Phase 2: Replace Hardcoded Strings (Priority: MEDIUM)

**Screens yang perlu diupdate:**

| Screen | Hardcoded Strings | Status |
|--------|-------------------|--------|
| DashboardScreen.kt | "Add Transaction", "Balance", greeting texts | Need Update |
| AddTransactionScreen.kt | "Add Transaction", "Amount", "Category", "Note", dll | Need Update |
| EditTransactionScreen.kt | "Edit Transaction", labels | Need Update |
| TransactionDetailScreen.kt | "Transaction Detail", buttons | Need Update |
| LoginScreen.kt | Form labels, buttons | Need Update |
| RegisterScreen.kt | Form labels, buttons | Need Update |
| SplashScreen.kt | App name, tagline | Need Update |
| StatisticsScreen.kt | Beberapa sudah pakai stringResource | Partial |
| NeoDatePicker.kt | Day names, month names | Need Update |
| CategoryUtils.kt | Category display names | Need Update |

**Task 2.1: Update all Screen composables**
- Replace hardcoded text dengan `stringResource(R.string.xxx)`
- Add missing string resources ke strings.xml

**Task 2.2: Update utility classes**
- CategoryUtils: gunakan context untuk get localized category names
- DateFormatter: gunakan locale-aware formatting
- CurrencyFormatter: sudah locale-aware (OK)

#### Phase 3: Testing & Polish (Priority: LOW)

**Task 3.1: Add more languages (optional)**
- values-ms/ untuk Bahasa Malaysia
- values-zh/ untuk Chinese

**Task 3.2: RTL Support**
- Test dengan Arabic/Hebrew jika diperlukan

**Task 3.3: Testing**
- Test switching language real-time
- Test persist language after app restart
- Test dengan device language berbeda

### Estimated Effort:
- Phase 1: 4-6 hours
- Phase 2: 3-4 hours
- Phase 3: 2-3 hours
- **Total: ~10-13 hours**

---

## 2. DAILY REMINDER NOTIFICATION FEATURE

### Current Implementation Status: ~95% Complete

#### ✅ Already Implemented:

| Component | Status | Description |
|-----------|--------|-------------|
| ReminderNotificationManager | ✅ Done | Manager class dengan semua fungsi notification |
| ReminderWorker | ✅ Done | WorkManager worker untuk scheduled notifications |
| Notification Channel | ✅ Done | Channel creation dengan proper config |
| AppPreferences | ✅ Done | Storage untuk reminder enabled, hour, minute |
| ProfileViewModel | ✅ Done | Logic untuk enable/disable dan set time |
| NeoTimePickerDialog | ✅ Done | UI untuk pilih waktu reminder |
| ProfileScreen UI | ✅ Done | Toggle dan time picker (saat ini di-hide) |
| ic_notification.xml | ✅ Done | Notification icon sudah ada |
| String Resources | ✅ Done | Notification title dan message dalam 2 bahasa |
| Permission Handling | ✅ Done | POST_NOTIFICATIONS permission untuk Android 13+ |
| WorkManager + Hilt | ✅ Done | HiltWorker integration sudah setup |

#### ❌ Missing/Incomplete:

| Component | Status | Description |
|-----------|--------|-------------|
| Boot Receiver | ❌ Missing | Reschedule reminder setelah device reboot |
| Exact Alarm Permission | ⚠️ Check | Android 12+ mungkin butuh SCHEDULE_EXACT_ALARM |
| Notification Action | ⚠️ Optional | Deep link ke Add Transaction screen |
| Testing | ❌ Not Done | Perlu testing real device |

### Implementation Plan:

#### Phase 1: Enable & Test Existing Implementation (Priority: HIGH)

**Task 1.1: Unhide ProfileScreen UI**
- Uncomment Language Section di ProfileScreen.kt
- Uncomment Reminder Section di ProfileScreen.kt
- Re-add removed imports

**Task 1.2: Test existing functionality**
- Test enable/disable reminder
- Test time picker
- Test notification muncul di waktu yang ditentukan
- Test notification click membuka app

#### Phase 2: Boot Receiver (Priority: MEDIUM)

**Task 2.1: Create BootReceiver**
```kotlin
// File: data/notification/BootReceiver.kt
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule reminder if enabled
        }
    }
}
```

**Task 2.2: Register in AndroidManifest.xml**
```xml
<receiver android:name=".data.notification.BootReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED"/>
    </intent-filter>
</receiver>

<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
```

#### Phase 3: Enhanced Notifications (Priority: LOW)

**Task 3.1: Add Notification Action Button**
- "Add Transaction" button di notification
- Deep link ke AddTransactionScreen

**Task 3.2: Smart Notification Content**
- Show today's expense summary di notification
- "You've spent Rp X today" atau "No expenses recorded today"

**Task 3.3: Notification Customization**
- Pilihan notification sound
- Pilihan vibration pattern
- Repeat options (weekdays only, everyday, custom)

### Estimated Effort:
- Phase 1: 1-2 hours
- Phase 2: 2-3 hours
- Phase 3: 3-4 hours
- **Total: ~6-9 hours**

---

## PRIORITY MATRIX

| Priority | Feature | Phase | Effort | Impact |
|----------|---------|-------|--------|--------|
| 🔴 HIGH | Notification - Enable & Test | Phase 1 | 1-2h | Immediate value |
| 🔴 HIGH | Localization - Core Infrastructure | Phase 1 | 4-6h | Required for feature |
| 🟡 MEDIUM | Notification - Boot Receiver | Phase 2 | 2-3h | Reliability |
| 🟡 MEDIUM | Localization - Replace Strings | Phase 2 | 3-4h | Complete feature |
| 🟢 LOW | Notification - Enhanced | Phase 3 | 3-4h | Nice to have |
| 🟢 LOW | Localization - More Languages | Phase 3 | 2-3h | Nice to have |

---

## RECOMMENDED IMPLEMENTATION ORDER

### Sprint 1 (Quick Win - Enable Features)
1. ✅ Unhide Notification Reminder UI di ProfileScreen
2. ✅ Test existing notification functionality
3. ✅ Fix any bugs found during testing

### Sprint 2 (Localization Core)
1. Create LocaleHelper utility
2. Update Application & MainActivity
3. Test language switching

### Sprint 3 (Complete Features)
1. Replace hardcoded strings across all screens
2. Add Boot Receiver untuk notification
3. Testing end-to-end

### Sprint 4 (Polish)
1. Enhanced notification features
2. Additional languages
3. Final testing & bug fixes

---

## TECHNICAL NOTES

### Localization Best Practices:
1. Selalu gunakan `stringResource()` di Compose, bukan hardcoded string
2. Untuk non-Compose code, gunakan `context.getString()`
3. Gunakan plurals untuk string dengan angka (e.g., "1 transaction" vs "5 transactions")
4. Format date/time dengan `DateTimeFormatter` yang locale-aware
5. Test dengan pseudo-locale untuk detect missing translations

### Notification Best Practices:
1. Selalu create notification channel di Application.onCreate()
2. Handle notification permission dengan graceful degradation
3. Use WorkManager untuk reliable scheduling (survive reboot dengan Boot Receiver)
4. Provide user control untuk enable/disable
5. Respect user's Do Not Disturb settings

---

## FILES TO MODIFY

### Localization:
- [ ] `MainActivity.kt` - Add locale wrapping
- [ ] `DuitTrackerApplication.kt` - Create atau update Application class
- [ ] `utils/LocaleHelper.kt` - Create new file
- [ ] `DashboardScreen.kt` - Replace hardcoded strings
- [ ] `AddTransactionScreen.kt` - Replace hardcoded strings
- [ ] `EditTransactionScreen.kt` - Replace hardcoded strings
- [ ] `TransactionDetailScreen.kt` - Replace hardcoded strings
- [ ] `LoginScreen.kt` - Replace hardcoded strings
- [ ] `RegisterScreen.kt` - Replace hardcoded strings
- [ ] `SplashScreen.kt` - Replace hardcoded strings
- [ ] `NeoDatePicker.kt` - Replace hardcoded day/month names
- [ ] `ProfileScreen.kt` - Unhide language section
- [ ] `res/values/strings.xml` - Add missing strings
- [ ] `res/values-id/strings.xml` - Add missing strings

### Notification:
- [ ] `ProfileScreen.kt` - Unhide reminder section
- [ ] `AndroidManifest.xml` - Add Boot Receiver & permission
- [ ] `data/notification/BootReceiver.kt` - Create new file
- [ ] `ReminderNotificationManager.kt` - Add action button (optional)

---

*Last Updated: January 2024*
