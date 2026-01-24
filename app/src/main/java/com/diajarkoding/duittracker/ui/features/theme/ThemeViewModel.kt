 package com.diajarkoding.duittracker.ui.features.theme
 
 import androidx.lifecycle.ViewModel
 import androidx.lifecycle.viewModelScope
 import com.diajarkoding.duittracker.data.local.preferences.AppPreferences
 import com.diajarkoding.duittracker.data.local.preferences.ThemeMode
 import dagger.hilt.android.lifecycle.HiltViewModel
 import kotlinx.coroutines.flow.SharingStarted
 import kotlinx.coroutines.flow.StateFlow
 import kotlinx.coroutines.flow.stateIn
 import kotlinx.coroutines.launch
 import javax.inject.Inject
 
 @HiltViewModel
 class ThemeViewModel @Inject constructor(
     private val appPreferences: AppPreferences
 ) : ViewModel() {
 
     val currentTheme: StateFlow<ThemeMode> = appPreferences.themeMode
         .stateIn(
             scope = viewModelScope,
             started = SharingStarted.WhileSubscribed(5000),
             initialValue = ThemeMode.SYSTEM
         )
 
     fun setTheme(mode: ThemeMode) {
         viewModelScope.launch {
             appPreferences.setThemeMode(mode)
         }
     }
 }
