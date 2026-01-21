package dev.koenv.rentmycar.shared.storage

import com.russhwolf.settings.Settings

/**
 * Factory to provide platform-specific Settings instance for local storage.
 */
expect object SettingsFactory {
    fun createSettings(): Settings
}
