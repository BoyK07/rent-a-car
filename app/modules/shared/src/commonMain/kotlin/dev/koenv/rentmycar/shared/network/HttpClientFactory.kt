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
 * Factory to create configured Ktor HTTP client with JWT authentication.
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
