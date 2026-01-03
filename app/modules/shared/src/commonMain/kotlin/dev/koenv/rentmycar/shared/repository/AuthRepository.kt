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
    
    init {
        checkAuthStatus()
    }
    
    /**
     * Checks if user is authenticated based on stored token.
     */
    private fun checkAuthStatus() {
        val hasToken = authTokenStorage.hasToken()
        _authState.value = if (hasToken) {
            AuthState.Authenticated
        } else {
            AuthState.Unauthenticated
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
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
    }
    
    /**
     * Checks if user is currently authenticated.
     */
    fun isAuthenticated(): Boolean {
        return authState.value == AuthState.Authenticated
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
