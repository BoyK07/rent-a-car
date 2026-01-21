package dev.koenv.rentmycar.shared.api

import dev.koenv.rentmycar.shared.dto.car.CarPhotoDto
import dev.koenv.rentmycar.shared.dto.car.CreateCarPhotoRequestDto
import dev.koenv.rentmycar.shared.dto.car.PatchCarPhotoRequestDto
import dev.koenv.rentmycar.shared.http.ApiResponse
import dev.koenv.rentmycar.shared.resources.ApiV1
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlin.uuid.Uuid

/**
 * API client for car photo endpoints.
 * NOTE: Backend implementation may be incomplete - verify endpoints before heavy use.
 */
class CarPhotoApi(
    private val httpClient: HttpClient
) {
    /**
     * Get all car photos.
     */
    suspend fun getCarPhotos(): Result<List<CarPhotoDto>> {
        return try {
            val response = httpClient.get(ApiV1.CarPhotos())
            val apiResponse = response.body<ApiResponse<List<CarPhotoDto>>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to fetch car photos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all photos for a specific car.
     */
    suspend fun getCarPhotosByCarId(carId: Uuid): Result<List<CarPhotoDto>> {
        return try {
            val response = httpClient.get(ApiV1.Cars.Id.Photos(parent = ApiV1.Cars.Id(id = carId.toString())))
            val apiResponse = response.body<ApiResponse<List<CarPhotoDto>>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to fetch car photos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get a single car photo by ID.
     */
    suspend fun getCarPhoto(id: Uuid): Result<CarPhotoDto> {
        return try {
            val response = httpClient.get(ApiV1.CarPhotos.Id(id = id.toString()))
            val apiResponse = response.body<ApiResponse<CarPhotoDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to fetch car photo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create a new car photo.
     */
    suspend fun createCarPhoto(request: CreateCarPhotoRequestDto): Result<CarPhotoDto> {
        return try {
            val response = httpClient.post(ApiV1.CarPhotos()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val apiResponse = response.body<ApiResponse<CarPhotoDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to create car photo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Partially update a car photo.
     */
    suspend fun patchCarPhoto(id: Uuid, request: PatchCarPhotoRequestDto): Result<CarPhotoDto> {
        return try {
            val response = httpClient.patch(ApiV1.CarPhotos.Id(id = id.toString())) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val apiResponse = response.body<ApiResponse<CarPhotoDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to patch car photo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a car photo.
     */
    suspend fun deleteCarPhoto(id: Uuid): Result<Unit> {
        return try {
            val response = httpClient.delete(ApiV1.CarPhotos.Id(id = id.toString()))
            val apiResponse = response.body<ApiResponse<Unit>>()
            if (apiResponse.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to delete car photo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload a photo file for a specific car.
     */
    suspend fun uploadCarPhoto(
        carId: Uuid,
        fileName: String,
        fileBytes: ByteArray
    ): Result<CarPhotoDto> {
        return try {
            val response = httpClient.post(
                ApiV1.Cars.Id.Photos.Upload(
                    parent = ApiV1.Cars.Id.Photos(parent = ApiV1.Cars.Id(id = carId.toString()))
                )
            ) {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                "file",
                                fileBytes,
                                Headers.build {
                                    append(HttpHeaders.ContentType, guessContentType(fileName).toString())
                                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                                }
                            )
                        }
                    )
                )
            }
            val apiResponse = response.body<ApiResponse<CarPhotoDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to upload car photo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun guessContentType(fileName: String): ContentType {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg" -> ContentType.Image.JPEG
            "png" -> ContentType.Image.PNG
            "webp" -> ContentType("image", "webp")
            "heic" -> ContentType("image", "heic")
            else -> ContentType.Application.OctetStream
        }
    }
}
