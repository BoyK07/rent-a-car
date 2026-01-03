package dev.koenv.rentmycar.shared.storage

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

/**
 * Android implementation of Settings factory using SharedPreferences.
 */
actual object SettingsFactory {
    private var context: Context? = null
    
    fun init(appContext: Context) {
        context = appContext
    }
    
    actual fun createSettings(): Settings {
        val ctx = context
        
        // If context is not available (e.g., in Android Studio preview mode),
        // return an in-memory settings implementation for preview compatibility
        return if (ctx != null) {
            val preferences = ctx.getSharedPreferences("rent_my_car_prefs", Context.MODE_PRIVATE)
            SharedPreferencesSettings(preferences)
        } else {
            // In-memory settings for preview mode
            InMemorySettings()
        }
    }
}

/**
 * In-memory Settings implementation for Android Studio preview mode.
 * This allows composables to be previewed without requiring an Application context.
 */
private class InMemorySettings : Settings {
    private val data = mutableMapOf<String, Any?>()
    
    override val keys: Set<String> get() = data.keys
    override val size: Int get() = data.size
    
    override fun clear() = data.clear()
    override fun remove(key: String) { data.remove(key) }
    override fun hasKey(key: String): Boolean = data.containsKey(key)
    
    override fun putInt(key: String, value: Int) { data[key] = value }
    override fun getInt(key: String, defaultValue: Int): Int = data[key] as? Int ?: defaultValue
    override fun getIntOrNull(key: String): Int? = data[key] as? Int
    
    override fun putLong(key: String, value: Long) { data[key] = value }
    override fun getLong(key: String, defaultValue: Long): Long = data[key] as? Long ?: defaultValue
    override fun getLongOrNull(key: String): Long? = data[key] as? Long
    
    override fun putString(key: String, value: String) { data[key] = value }
    override fun getString(key: String, defaultValue: String): String = data[key] as? String ?: defaultValue
    override fun getStringOrNull(key: String): String? = data[key] as? String
    
    override fun putFloat(key: String, value: Float) { data[key] = value }
    override fun getFloat(key: String, defaultValue: Float): Float = data[key] as? Float ?: defaultValue
    override fun getFloatOrNull(key: String): Float? = data[key] as? Float
    
    override fun putDouble(key: String, value: Double) { data[key] = value }
    override fun getDouble(key: String, defaultValue: Double): Double = data[key] as? Double ?: defaultValue
    override fun getDoubleOrNull(key: String): Double? = data[key] as? Double
    
    override fun putBoolean(key: String, value: Boolean) { data[key] = value }
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = data[key] as? Boolean ?: defaultValue
    override fun getBooleanOrNull(key: String): Boolean? = data[key] as? Boolean
}
