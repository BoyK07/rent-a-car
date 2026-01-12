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
import dev.koenv.rentmycar.shared.storage.ThemePreferences
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
    private val _themePreferences: ThemePreferences by lazy { ThemePreferences() }
    
    // HTTP Client - mutable so it can be recreated on logout/login
    private var _httpClient: HttpClient? = null
    private val httpClient: HttpClient
        get() {
            if (_httpClient == null) {
                _httpClient = HttpClientFactory.create(baseUrl, _authTokenStorage)
            }
            return _httpClient!!
        }
    
    // API Clients - mutable so they can be recreated with new HTTP client
    private var _authApi: AuthApi? = null
    private val authApi: AuthApi
        get() {
            if (_authApi == null) {
                _authApi = AuthApi(httpClient)
            }
            return _authApi!!
        }
    
    private var _carsApi: CarsApi? = null
    private val carsApi: CarsApi
        get() {
            if (_carsApi == null) {
                _carsApi = CarsApi(httpClient)
            }
            return _carsApi!!
        }
    
    private var _userApi: UserApi? = null
    private val userApi: UserApi
        get() {
            if (_userApi == null) {
                _userApi = UserApi(httpClient)
            }
            return _userApi!!
        }
    
    private var _reservationApi: ReservationApi? = null
    private val reservationApi: ReservationApi
        get() {
            if (_reservationApi == null) {
                _reservationApi = ReservationApi(httpClient)
            }
            return _reservationApi!!
        }
    
    private var __carPhotoApi: CarPhotoApi? = null
    private val _carPhotoApi: CarPhotoApi
        get() {
            if (__carPhotoApi == null) {
                __carPhotoApi = CarPhotoApi(httpClient)
            }
            return __carPhotoApi!!
        }
    
    private var __carAvailabilityApi: CarAvailabilityApi? = null
    private val _carAvailabilityApi: CarAvailabilityApi
        get() {
            if (__carAvailabilityApi == null) {
                __carAvailabilityApi = CarAvailabilityApi(httpClient)
            }
            return __carAvailabilityApi!!
        }
    
    // DAOs - lazy initialized after database is ready
    private val carDao: CarDao by lazy { CarDao(databaseManager) }
    private val userDao: UserDao by lazy { UserDao(databaseManager) }
    private val reservationDao: ReservationDao by lazy { ReservationDao(databaseManager) }
    
    // Public DAO access for UI layer
    fun provideCarDao(): CarDao = carDao
    fun provideUserDao(): UserDao = userDao
    fun provideReservationDao(): ReservationDao = reservationDao
    
    // Repositories - mutable so they can be recreated with new API clients
    private var _authRepository: AuthRepository? = null
    val authRepository: AuthRepository
        get() {
            if (_authRepository == null) {
                _authRepository = AuthRepository(authApi, _authTokenStorage)
            }
            return _authRepository!!
        }
    
    private var _carsRepository: CarsRepository? = null
    val carsRepository: CarsRepository
        get() {
            if (_carsRepository == null) {
                _carsRepository = CarsRepository(carsApi, carDao, _appDataStorage)
            }
            return _carsRepository!!
        }
    
    private var _userRepository: UserRepository? = null
    val userRepository: UserRepository
        get() {
            if (_userRepository == null) {
                _userRepository = UserRepository(userApi, userDao)
            }
            return _userRepository!!
        }
    
    private var _reservationRepository: ReservationRepository? = null
    val reservationRepository: ReservationRepository
        get() {
            if (_reservationRepository == null) {
                _reservationRepository = ReservationRepository(reservationApi, reservationDao)
            }
            return _reservationRepository!!
        }
    
    // API Clients (public access for direct use when repository doesn't exist)
    val carPhotoApi: CarPhotoApi get() = _carPhotoApi
    val carAvailabilityApi: CarAvailabilityApi get() = _carAvailabilityApi
    
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
     * Provides access to theme preferences for UI layer.
     */
    fun getThemePreferences(): ThemePreferences = _themePreferences
    
    /**
     * Provides access to the HTTP client for external use (e.g., image loading).
     * This is the same authenticated client used by all API calls.
     */
    fun provideHttpClient(): HttpClient = httpClient
    
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
    
    /**
     * Recreates the HTTP client, all API clients, and repositories to force reloading of auth tokens.
     * Should be called after login/logout to ensure the new token is used.
     * This is necessary because Ktor's Auth plugin caches tokens.
     */
    fun recreateHttpClient() {
        _httpClient?.close()
        _httpClient = null
        
        // Clear all API clients so they get recreated with the new HTTP client
        _authApi = null
        _carsApi = null
        _userApi = null
        _reservationApi = null
        __carPhotoApi = null
        __carAvailabilityApi = null
        
        // Clear all repositories so they get recreated with the new API clients
        _authRepository = null
        _carsRepository = null
        _userRepository = null
        _reservationRepository = null
        
        // Next access to httpClient, API clients, and repositories will create new instances
    }
}
