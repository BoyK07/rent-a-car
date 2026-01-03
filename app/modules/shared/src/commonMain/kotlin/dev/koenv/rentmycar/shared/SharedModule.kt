package dev.koenv.rentmycar.shared

import dev.koenv.rentmycar.shared.api.AuthApi
import dev.koenv.rentmycar.shared.api.CarsApi
import dev.koenv.rentmycar.shared.api.UserApi
import dev.koenv.rentmycar.shared.network.HttpClientFactory
import dev.koenv.rentmycar.shared.repository.AuthRepository
import dev.koenv.rentmycar.shared.repository.CarsRepository
import dev.koenv.rentmycar.shared.repository.UserRepository
import dev.koenv.rentmycar.shared.storage.AppDataStorage
import dev.koenv.rentmycar.shared.storage.AuthTokenStorage
import io.ktor.client.*

/**
 * Shared module dependencies container.
 * Provides singleton instances of repositories, APIs, and storage.
 */
object SharedModule {
    // Default base URL - uses localhost for development
    // Configure for production using configure() method or environment detection
    private var baseUrl: String = detectBaseUrl()
    
    // Storage
    private val _authTokenStorage: AuthTokenStorage by lazy { AuthTokenStorage() }
    private val _appDataStorage: AppDataStorage by lazy { AppDataStorage() }
    
    // HTTP Client
    private val httpClient: HttpClient by lazy {
        HttpClientFactory.create(baseUrl, _authTokenStorage)
    }
    
    // API Clients
    private val authApi: AuthApi by lazy { AuthApi(httpClient) }
    private val carsApi: CarsApi by lazy { CarsApi(httpClient) }
    private val userApi: UserApi by lazy { UserApi(httpClient) }
    
    // Repositories (public access)
    val authRepository: AuthRepository by lazy { AuthRepository(authApi, _authTokenStorage) }
    val carsRepository: CarsRepository by lazy { CarsRepository(carsApi, _appDataStorage) }
    val userRepository: UserRepository by lazy { UserRepository(userApi) }
    
    /**
     * Detects the appropriate base URL based on environment.
     * Returns localhost for development, production URL for release builds.
     */
    private fun detectBaseUrl(): String {
        return "http://localhost:8080"
        
        // return if (BuildConfig.DEBUG) {
        //     "http://localhost:8080"
        //     "http://10.0.2.2:8080" // Android emulator
        // } else {
        //     "https://api.rentmycar.com"
        // }
    }
    
    /**
     * Configures the base URL for API calls.
     * Should be called at app startup before accessing any repositories.
     * 
     * For local development with emulator, use:
     * ```
     * SharedModule.configure("http://10.0.2.2:8080")  // Android emulator
     * ```
     * 
     * For production:
     * ```
     * SharedModule.configure("https://api.rentmycar.com")
     * ```
     */
    fun configure(serverUrl: String) {
        baseUrl = serverUrl
    }
    
    /**
     * Provides access to app data storage for UI layer if needed.
     */
    fun getAppDataStorage(): AppDataStorage = _appDataStorage
}
