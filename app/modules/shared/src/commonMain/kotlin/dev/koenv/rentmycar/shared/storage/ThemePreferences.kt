package dev.koenv.rentmycar.shared.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Storage for theme preferences.
 * Manages dark mode settings across app restarts.
 */
class ThemePreferences(
    private val settings: Settings = SettingsFactory.createSettings()
) {
    companion object {
        private const val KEY_DARK_MODE = "theme_dark_mode"
    }
    
    private val _darkModeFlow = MutableStateFlow<Boolean?>(settings.getBooleanOrNull(KEY_DARK_MODE))
    val darkModeFlow: StateFlow<Boolean?> = _darkModeFlow.asStateFlow()
    
    /**
     * Saves the dark mode preference.
     */
    fun setDarkMode(enabled: Boolean) {
        settings[KEY_DARK_MODE] = enabled
        _darkModeFlow.value = enabled
    }
    
    /**
     * Retrieves the dark mode preference.
     * Returns null if not set (defaults to system setting).
     */
    fun isDarkMode(): Boolean? {
        return settings.getBooleanOrNull(KEY_DARK_MODE)
    }
    
    /**
     * Clears the dark mode preference.
     */
    fun clearDarkMode() {
        settings.remove(KEY_DARK_MODE)
        _darkModeFlow.value = null
    }
}
