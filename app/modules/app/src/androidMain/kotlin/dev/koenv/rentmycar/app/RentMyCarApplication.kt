package dev.koenv.rentmycar.app

import android.app.Application
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.db.DatabaseDriverFactory
import dev.koenv.rentmycar.shared.storage.SettingsFactory

/**
 * Android Application class for Rent My Car.
 * Initializes shared module components including database and settings storage.
 * Configures API base URL from BuildConfig (environment-aware: debug/release).
 */
class RentMyCarApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        SettingsFactory.init(applicationContext)
        SharedModule.initialize(DatabaseDriverFactory(applicationContext))
        SharedModule.configure(BuildConfig.API_BASE_URL)
    }
}
