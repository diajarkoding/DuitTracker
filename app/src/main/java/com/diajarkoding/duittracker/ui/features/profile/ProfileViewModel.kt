 package com.diajarkoding.duittracker.ui.features.profile
 
 import androidx.lifecycle.ViewModel
 import androidx.lifecycle.viewModelScope
 import com.diajarkoding.duittracker.data.local.preferences.AppLanguage
 import com.diajarkoding.duittracker.data.local.preferences.AppPreferences
 import com.diajarkoding.duittracker.data.notification.ReminderNotificationManager
 import com.diajarkoding.duittracker.domain.repository.IAuthRepository
 import com.diajarkoding.duittracker.ui.components.SnackbarType
 import dagger.hilt.android.lifecycle.HiltViewModel
 import kotlinx.coroutines.flow.MutableSharedFlow
 import kotlinx.coroutines.flow.MutableStateFlow
 import kotlinx.coroutines.flow.SharedFlow
 import kotlinx.coroutines.flow.StateFlow
 import kotlinx.coroutines.flow.asSharedFlow
 import kotlinx.coroutines.flow.asStateFlow
 import kotlinx.coroutines.flow.combine
 import kotlinx.coroutines.flow.first
 import kotlinx.coroutines.flow.update
 import kotlinx.coroutines.launch
 import javax.inject.Inject
 
 data class ProfileUiState(
     val userName: String = "",
     val email: String = "",
     val avatarUrl: String? = null,
     val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
     val isReminderEnabled: Boolean = false,
     val reminderHour: Int = 20,
     val reminderMinute: Int = 0,
     val isLoading: Boolean = false,
     val showTimePickerDialog: Boolean = false,
     val showLogoutDialog: Boolean = false
 )
 
 sealed class ProfileEvent {
     data object LoggedOut : ProfileEvent()
     data class ShowSnackbar(val message: String, val type: SnackbarType = SnackbarType.INFO) : ProfileEvent()
     data object LanguageChanged : ProfileEvent()
 }
 
 @HiltViewModel
 class ProfileViewModel @Inject constructor(
     private val authRepository: IAuthRepository,
     private val appPreferences: AppPreferences,
     private val reminderManager: ReminderNotificationManager
 ) : ViewModel() {
 
     private val _uiState = MutableStateFlow(ProfileUiState())
     val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
 
     private val _events = MutableSharedFlow<ProfileEvent>()
     val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()
 
     init {
         loadUserData()
         observePreferences()
         reminderManager.createNotificationChannel()
     }
 
     private fun loadUserData() {
         viewModelScope.launch {
             authRepository.currentUser.collect { user ->
                 _uiState.update {
                     it.copy(
                         userName = user?.name ?: "",
                         email = user?.email ?: "",
                         avatarUrl = user?.avatarUrl
                     )
                 }
             }
         }
     }
 
     private fun observePreferences() {
         viewModelScope.launch {
             combine(
                 appPreferences.language,
                 appPreferences.isReminderEnabled,
                 appPreferences.reminderHour,
                 appPreferences.reminderMinute
             ) { language, reminderEnabled, hour, minute ->
                 ProfileUiState(
                     selectedLanguage = language,
                     isReminderEnabled = reminderEnabled,
                     reminderHour = hour,
                     reminderMinute = minute
                 )
             }.collect { prefs ->
                 _uiState.update {
                     it.copy(
                         selectedLanguage = prefs.selectedLanguage,
                         isReminderEnabled = prefs.isReminderEnabled,
                         reminderHour = prefs.reminderHour,
                         reminderMinute = prefs.reminderMinute
                     )
                 }
             }
         }
     }
 
     fun setLanguage(language: AppLanguage) {
         viewModelScope.launch {
             appPreferences.setLanguage(language)
             _events.emit(ProfileEvent.LanguageChanged)
         }
     }
 
     fun setReminderEnabled(enabled: Boolean) {
         viewModelScope.launch {
             appPreferences.setReminderEnabled(enabled)
             if (enabled) {
                 val hour = appPreferences.reminderHour.first()
                 val minute = appPreferences.reminderMinute.first()
                 reminderManager.scheduleReminder(hour, minute)
                 _events.emit(ProfileEvent.ShowSnackbar("Reminder enabled", SnackbarType.SUCCESS))
             } else {
                 reminderManager.cancelReminder()
                 _events.emit(ProfileEvent.ShowSnackbar("Reminder disabled", SnackbarType.INFO))
             }
         }
     }
 
     fun showTimePickerDialog() {
         _uiState.update { it.copy(showTimePickerDialog = true) }
     }
 
     fun hideTimePickerDialog() {
         _uiState.update { it.copy(showTimePickerDialog = false) }
     }
 
     fun setReminderTime(hour: Int, minute: Int) {
         viewModelScope.launch {
             appPreferences.setReminderTime(hour, minute)
             if (appPreferences.isReminderEnabled.first()) {
                 reminderManager.scheduleReminder(hour, minute)
             }
             _uiState.update { it.copy(showTimePickerDialog = false) }
             _events.emit(ProfileEvent.ShowSnackbar(
                 "Reminder set to ${String.format("%02d:%02d", hour, minute)}",
                 SnackbarType.SUCCESS
             ))
         }
     }
 
     fun showLogoutDialog() {
         _uiState.update { it.copy(showLogoutDialog = true) }
     }
 
     fun hideLogoutDialog() {
         _uiState.update { it.copy(showLogoutDialog = false) }
     }
 
     fun logout() {
         viewModelScope.launch {
             _uiState.update { it.copy(isLoading = true, showLogoutDialog = false) }
             authRepository.logout()
             reminderManager.cancelReminder()
             _events.emit(ProfileEvent.LoggedOut)
         }
     }
 
     fun hasNotificationPermission(): Boolean {
         return reminderManager.hasNotificationPermission()
     }
 }
