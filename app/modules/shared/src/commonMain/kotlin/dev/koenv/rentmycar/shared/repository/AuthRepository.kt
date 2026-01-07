package dev.koenv.rentmycar.shared.repository

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
 * Repository for authentication operations.
 * Manages auth state, token storage, and API calls.
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
     * Checks if user is authenticated based on stored token.
     * Also attempts to restore user data from storage.
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
        }.onFailure {
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    /**
     * Logs out the current user.
     */
    fun logout() {
        authTokenStorage.clearToken()
        authTokenStorage.clearUserData()
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
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
