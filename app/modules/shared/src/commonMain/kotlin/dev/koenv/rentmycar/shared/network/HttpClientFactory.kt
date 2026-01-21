package dev.koenv.rentmycar.shared.network

import dev.koenv.rentmycar.shared.storage.AuthTokenStorage
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Factory for creating configured Ktor HTTP client instances.
 * 
 * Client configuration includes:
 * - Base URL setting (environment-aware: debug/release)
 * - JWT Bearer authentication with automatic token injection
 * - JSON content negotiation with lenient parsing
 * - Request/response logging for debugging
 * - Timeout configuration (30s request, 15s connect)
 * - Type-safe routing with Resources plugin
 * 
 * Authentication flow:
 * - Loads token from AuthTokenStorage for each request
 * - Automatically adds Authorization: Bearer {token} header
 * - No refresh token support (user must re-login on expiry)
 */
object HttpClientFactory {
    
    fun create(
        baseUrl: String,
        authTokenStorage: AuthTokenStorage
    ): HttpClient {
        return HttpClient(HttpEngineFactory.createEngine()) {
            // Base URL configuration
            defaultRequest {
                url(baseUrl)
            }
            
            // Type-safe routing with Resources
            install(Resources)
            
            // JSON content negotiation
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            
            // Authentication with JWT
            install(Auth) {
                bearer {
                    loadTokens {
                        authTokenStorage.getToken()?.let { token ->
                            BearerTokens(accessToken = token, refreshToken = "")
                        }
                    }
                    
                    refreshTokens {
                        // No refresh token in this simplified implementation
                        // If token expires, user needs to login again
                        null
                    }
                }
            }
            
            // Logging for debugging
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
            
            // Timeout configuration
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 30_000
            }
        }
    }
}
