package dev.koenv.rentmycar.shared.storage

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

/**
 * JVM/Desktop implementation of Settings factory using Java Preferences API.
 */
actual object SettingsFactory {
    actual fun createSettings(): Settings {
        val preferences = Preferences.userRoot().node("dev.koenv.rentmycar")
        return PreferencesSettings(preferences)
    }
}
