package dev.koenv.rentmycar.shared.api

import dev.koenv.rentmycar.shared.dto.user.CreateUserRequestDto
import dev.koenv.rentmycar.shared.dto.user.PatchUserRequestDto
import dev.koenv.rentmycar.shared.dto.user.UpdateUserRequestDto
import dev.koenv.rentmycar.shared.dto.user.UserDto
import dev.koenv.rentmycar.shared.http.ApiResponse
import dev.koenv.rentmycar.shared.resources.ApiV1
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.uuid.Uuid

/**
 * API client for user endpoints.
 */
class UserApi(
    private val httpClient: HttpClient
) {
    /**
     * Fetches the current user's profile by their ID.
     * Note: The server requires the user ID, not a "me" endpoint.
     */
    suspend fun getCurrentUser(userId: Uuid): Result<UserDto> {
        return try {
            val response = httpClient.get(ApiV1.Users.Id(id = userId.toString()))
            val apiResponse = response.body<ApiResponse<UserDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to fetch user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Fetches all users (admin only).
     */
    suspend fun getAllUsers(): Result<List<UserDto>> {
        return try {
            val response = httpClient.get(ApiV1.Users())
            val apiResponse = response.body<ApiResponse<List<UserDto>>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to fetch users"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Deletes a user by ID (admin only).
     */
    suspend fun deleteUser(userId: Uuid): Result<Unit> {
        return try {
            val response = httpClient.delete(ApiV1.Users.Id(id = userId.toString()))
            val apiResponse = response.body<ApiResponse<Unit>>()
            if (apiResponse.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to delete user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Creates a new user (admin only).
     */
    suspend fun createUser(request: CreateUserRequestDto): Result<UserDto> {
        return try {
            val response = httpClient.post(ApiV1.Users()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val apiResponse = response.body<ApiResponse<UserDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to create user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Updates an existing user (admin only).
     */
    suspend fun updateUser(userId: Uuid, request: UpdateUserRequestDto): Result<UserDto> {
        return try {
            val response = httpClient.put(ApiV1.Users.Id(id = userId.toString())) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val apiResponse = response.body<ApiResponse<UserDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to update user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Partially updates an existing user (admin only).
     */
    suspend fun patchUser(userId: Uuid, request: PatchUserRequestDto): Result<UserDto> {
        return try {
            val response = httpClient.patch(ApiV1.Users.Id(id = userId.toString())) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val apiResponse = response.body<ApiResponse<UserDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to patch user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

