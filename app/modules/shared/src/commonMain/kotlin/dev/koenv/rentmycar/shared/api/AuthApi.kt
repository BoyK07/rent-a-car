package dev.koenv.rentmycar.shared.api

import dev.koenv.rentmycar.shared.dto.auth.AuthResponseDto
import dev.koenv.rentmycar.shared.dto.auth.LoginRequestDto
import dev.koenv.rentmycar.shared.dto.auth.RegisterRequestDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * API client for authentication endpoints.
 */
class AuthApi(
    private val httpClient: HttpClient
) {
    /**
     * Registers a new user.
     */
    suspend fun register(request: RegisterRequestDto): Result<AuthResponseDto> {
        return try {
            val response = httpClient.post("/api/v1/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Logs in a user.
     */
    suspend fun login(request: LoginRequestDto): Result<AuthResponseDto> {
        return try {
            val response = httpClient.post("/api/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
