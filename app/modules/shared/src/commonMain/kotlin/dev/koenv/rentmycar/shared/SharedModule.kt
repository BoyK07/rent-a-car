package dev.koenv.rentmycar.shared

import dev.koenv.rentmycar.shared.api.AuthApi
import dev.koenv.rentmycar.shared.api.CarAvailabilityApi
import dev.koenv.rentmycar.shared.api.CarPhotoApi
import dev.koenv.rentmycar.shared.api.CarsApi
import dev.koenv.rentmycar.shared.api.ReservationApi
import dev.koenv.rentmycar.shared.api.UserApi
import dev.koenv.rentmycar.shared.db.DatabaseManager
import dev.koenv.rentmycar.shared.db.dao.CarDao
import dev.koenv.rentmycar.shared.db.dao.ReservationDao
import dev.koenv.rentmycar.shared.db.dao.UserDao
import dev.koenv.rentmycar.shared.network.HttpClientFactory
import dev.koenv.rentmycar.shared.repository.AuthRepository
import dev.koenv.rentmycar.shared.repository.CarsRepository
import dev.koenv.rentmycar.shared.repository.ReservationRepository
import dev.koenv.rentmycar.shared.repository.UserRepository
import dev.koenv.rentmycar.shared.storage.AppDataStorage
import dev.koenv.rentmycar.shared.storage.AuthTokenStorage
import dev.koenv.rentmycar.shared.state.FilterState
import io.ktor.client.*

/**
 * Shared module dependencies container.
 * Provides singleton instances of repositories, APIs, storage, and database.
 * 
 * IMPORTANT: Call initialize() with platform context before using repositories.
 */
object SharedModule {
    // Persistent filter state (survives navigation)
    val filterState: FilterState = FilterState()
    // Default base URL - uses localhost for development
    // Configure for production using configure() method or environment detection
    private var baseUrl: String = detectBaseUrl()
    
    // Database - must be initialized with platform context
    private var _databaseManager: DatabaseManager? = null
    val databaseManager: DatabaseManager
        get() = _databaseManager ?: error("Database not initialized. Call SharedModule.initialize() first.")
    
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
    private val reservationApi: ReservationApi by lazy { ReservationApi(httpClient) }
    private val carPhotoApi: CarPhotoApi by lazy { CarPhotoApi(httpClient) }
    private val carAvailabilityApi: CarAvailabilityApi by lazy { CarAvailabilityApi(httpClient) }
    
    // DAOs - lazy initialized after database is ready
    private val carDao: CarDao by lazy { CarDao(databaseManager) }
    private val userDao: UserDao by lazy { UserDao(databaseManager) }
    private val reservationDao: ReservationDao by lazy { ReservationDao(databaseManager) }
    
    // Public DAO access for UI layer
    fun provideCarDao(): CarDao = carDao
    fun provideUserDao(): UserDao = userDao
    fun provideReservationDao(): ReservationDao = reservationDao
    
    // Repositories (public access)
    val authRepository: AuthRepository by lazy { AuthRepository(authApi, _authTokenStorage) }
    val carsRepository: CarsRepository by lazy { CarsRepository(carsApi, carDao, _appDataStorage) }
    val userRepository: UserRepository by lazy { UserRepository(userApi, userDao) }
    val reservationRepository: ReservationRepository by lazy { ReservationRepository(reservationApi, reservationDao) }
    
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
    
    /**
     * Initializes the shared module with platform-specific database driver.
     * MUST be called once at app startup before accessing repositories.
     * 
     * @param databaseDriverFactory Platform-specific database driver factory
     */
    fun initialize(databaseDriverFactory: dev.koenv.rentmycar.shared.db.DatabaseDriverFactory) {
        _databaseManager = DatabaseManager(databaseDriverFactory)
    }
    
    /**
     * Check if the module has been initialized with a database.
     */
    fun isInitialized(): Boolean = _databaseManager != null
}
