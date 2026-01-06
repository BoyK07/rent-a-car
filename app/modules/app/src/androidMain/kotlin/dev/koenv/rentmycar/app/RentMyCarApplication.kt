package dev.koenv.rentmycar.app

import android.app.Application
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.db.DatabaseDriverFactory
import dev.koenv.rentmycar.shared.storage.SettingsFactory

class RentMyCarApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize platform-specific storage
        SettingsFactory.init(applicationContext)
        
        // Initialize database
        SharedModule.initialize(DatabaseDriverFactory(applicationContext))
        
        // Configure API base URL
        configureApiUrl()
    }
    
    private fun configureApiUrl() {
        // Development: `localhost` and `10.0.2.2` allowed for local development
        SharedModule.configure("http://10.0.2.2:8080")
        // SharedModule.configure("http://localhost:8080")
        
        // Production configuration:
        // SharedModule.configure("https://api.rentmycar.com")
    }
}
