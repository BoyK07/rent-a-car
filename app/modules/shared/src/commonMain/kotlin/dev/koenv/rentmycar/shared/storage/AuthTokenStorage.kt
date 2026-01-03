package dev.koenv.rentmycar.shared.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import com.russhwolf.settings.get

/**
 * Storage for authentication token.
 * Manages JWT token persistence across app restarts.
 */
class AuthTokenStorage(
    private val settings: Settings = SettingsFactory.createSettings()
) {
    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
    }
    
    /**
     * Saves the authentication token.
     */
    fun saveToken(token: String) {
        settings[KEY_AUTH_TOKEN] = token
    }
    
    /**
     * Retrieves the saved authentication token, or null if not found.
     */
    fun getToken(): String? {
        return settings.getStringOrNull(KEY_AUTH_TOKEN)
    }
    
    /**
     * Checks if a valid token exists.
     */
    fun hasToken(): Boolean {
        return getToken() != null
    }
    
    /**
     * Clears the stored authentication token.
     */
    fun clearToken() {
        settings.remove(KEY_AUTH_TOKEN)
    }
}
