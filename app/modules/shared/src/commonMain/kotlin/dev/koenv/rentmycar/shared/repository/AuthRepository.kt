package dev.koenv.rentmycar.shared.repository

import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.api.AuthApi
import dev.koenv.rentmycar.shared.dto.auth.AuthResponseDto
import dev.koenv.rentmycar.shared.dto.auth.LoginRequestDto
import dev.koenv.rentmycar.shared.dto.auth.RegisterRequestDto
import dev.koenv.rentmycar.shared.dto.user.UserDto
import dev.koenv.rentmycar.shared.storage.AuthTokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository managing user authentication state and operations.
 * 
 * Features:
 * - Login/logout with JWT token management
 * - User registration with optional role selection
 * - Authentication state tracking (Loading, Authenticated, Unauthenticated)
 * - Current user state flow
 * - Session persistence across app restarts
 * - Automatic token inclusion in HTTP requests
 * - User data storage/restoration
 * 
 * Authentication flow:
 * 1. User logs in → token saved to storage → auth state = Authenticated
 * 2. App restart → token loaded from storage → user data restored
 * 3. User logs out → token cleared → auth state = Unauthenticated
 */
class AuthRepository(
    private val authApi: AuthApi,
    private val authTokenStorage: AuthTokenStorage
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<UserDto?>(null)
    val currentUser: StateFlow<UserDto?> = _currentUser.asStateFlow()
    
    private val json = Json { ignoreUnknownKeys = true }
    
    companion object {
        private const val USER_STORAGE_KEY = "current_user"
    }
    
    init {
        checkAuthStatus()
    }
    
    /**
     * Validates authentication status on repository initialization.
     * Attempts to restore user session from stored token and user data.
     */
    private fun checkAuthStatus() {
        val hasToken = authTokenStorage.hasToken()
        if (hasToken) {
            // Try to restore user from storage
            try {
                val userJson = authTokenStorage.getUserData()
                if (userJson != null) {
                    val user = json.decodeFromString<UserDto>(userJson)
                    _currentUser.value = user
                }
            } catch (e: Exception) {
                // User data couldn't be restored, but token exists
                // User will need to be fetched from API or re-login
            }
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    /**
     * Restores user session from stored data.
     * Returns the user if available, or error if not found.
     */
    suspend fun restoreUserSession(): Result<UserDto> {
        return try {
            val userJson = authTokenStorage.getUserData()
            if (userJson != null) {
                val user = json.decodeFromString<UserDto>(userJson)
                _currentUser.value = user
                Result.success(user)
            } else {
                Result.failure(Exception("No user data found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Registers a new user.
     */
    suspend fun register(request: RegisterRequestDto): Result<AuthResponseDto> {
        _authState.value = AuthState.Loading
        
        return authApi.register(request).onSuccess { response ->
            authTokenStorage.saveToken(response.token)
            _currentUser.value = response.user
            // Persist user data
            authTokenStorage.saveUserData(json.encodeToString(response.user))
            _authState.value = AuthState.Authenticated
            // Recreate HTTP client to use the new token
            SharedModule.recreateHttpClient()
        }.onFailure {
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    /**
     * Logs in a user.
     */
    suspend fun login(request: LoginRequestDto): Result<AuthResponseDto> {
        _authState.value = AuthState.Loading
        
        return authApi.login(request).onSuccess { response ->
            authTokenStorage.saveToken(response.token)
            _currentUser.value = response.user
            // Persist user data
            authTokenStorage.saveUserData(json.encodeToString(response.user))
            _authState.value = AuthState.Authenticated
            // Recreate HTTP client to use the new token
            SharedModule.recreateHttpClient()
        }.onFailure {
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    /**
     * Logs out the current user and clears all local data.
     * Ensures complete data cleanup including:
     * - Authentication tokens and user data
     * - All cached database records (cars, reservations, users)
     * - App data (viewed cars, preferences)
     * - HTTP client (to clear cached auth tokens)
     */
    fun logout() {
        // Clear auth data (token + user data)
        authTokenStorage.clearAll()
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
        
        // Recreate HTTP client to clear cached auth tokens from Ktor's Auth plugin
        SharedModule.recreateHttpClient()
        
        // Clear all cached data from local database
        try {
            SharedModule.databaseManager.clearAllData()
        } catch (e: Exception) {
            // Ignore errors during cleanup
        }
        
        // Clear app data storage (viewed cars, preferences, etc.)
        try {
            SharedModule.getAppDataStorage().clearAll()
        } catch (e: Exception) {
            // Ignore errors during cleanup
        }
    }
    
    /**
     * Checks if user is currently authenticated.
     */
    fun isAuthenticated(): Boolean {
        return authState.value == AuthState.Authenticated
    }
    
    /**
     * Updates the current user data after a profile update.
     * Used when user edits their profile.
     */
    fun updateCurrentUser(user: UserDto) {
        _currentUser.value = user
        authTokenStorage.saveUserData(json.encodeToString(user))
    }
}

/**
 * Represents the authentication state of the user.
 */
sealed class AuthState {
    data object Loading : AuthState()
    data object Authenticated : AuthState()
    data object Unauthenticated : AuthState()
}
