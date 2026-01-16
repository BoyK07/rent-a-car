package dev.koenv.rentmycar.server.routes.api.v1.cars

/**
 * Car photo management API routes.
 * 
 * Endpoints:
 * - POST /api/v1/cars/{carId}/photos - Upload photo (owner or admin only)
 * - GET /api/v1/cars/{carId}/photos - List all photos for car (public)
 * - GET /api/v1/cars/{carId}/photos/{id} - Get specific photo (public)
 * - DELETE /api/v1/cars/{carId}/photos/{id} - Delete photo (owner or admin only)
 * - PATCH /api/v1/cars/{carId}/photos/{id}/primary - Set as primary photo (owner or admin only)
 */

import dev.koenv.rentmycar.server.domain.service.CarPhotoService
import dev.koenv.rentmycar.server.domain.service.CarService
import dev.koenv.rentmycar.server.mappers.car.applyPatch
import dev.koenv.rentmycar.server.mappers.car.toDto
import dev.koenv.rentmycar.server.mappers.car.toEntity
import dev.koenv.rentmycar.server.routes.RouteRegistrar
import dev.koenv.rentmycar.server.util.requireBodyOrFail
import dev.koenv.rentmycar.server.util.requireRole
import dev.koenv.rentmycar.server.util.respondError
import dev.koenv.rentmycar.server.util.respondSuccess
import dev.koenv.rentmycar.server.util.respondCreated
import dev.koenv.rentmycar.server.util.verifyOwnership
import dev.koenv.rentmycar.shared.domain.entity.CarPhoto
import dev.koenv.rentmycar.shared.domain.enums.Role
import dev.koenv.rentmycar.shared.dto.car.CreateCarPhotoRequestDto
import dev.koenv.rentmycar.shared.dto.car.PatchCarPhotoRequestDto
import dev.koenv.rentmycar.shared.http.ApiException
import dev.koenv.rentmycar.shared.resources.ApiV1
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.request.*
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.patch
import io.ktor.server.resources.delete
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import org.koin.ktor.ext.inject
import java.io.File
import kotlin.uuid.Uuid

object CarPhotoRoutes : RouteRegistrar {
    override fun Route.register() {
        val photoService by inject<CarPhotoService>()
        val carService by inject<CarService>()

        authenticate("auth-jwt") {
            // GET /api/v1/cars/{id}/photos - List photos for a car
            get<ApiV1.Cars.Id.Photos> { resource ->
                call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                
                val carId = try {
                    Uuid.parse(resource.parent.id)
                } catch (_: IllegalArgumentException) {
                    return@get call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid carId format",
                        "INVALID_CAR_ID",
                        call.callId
                    )
                }
                
                val items = photoService.getByCarId(carId)
                call.respondSuccess(items.map { it.toDto() })
            }

            // GET /api/v1/cars/{id}/photos/{photoId} - Get specific photo
            get<ApiV1.Cars.Id.Photos.PhotoId> { resource ->
                call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                
                val carId = try {
                    Uuid.parse(resource.parent.parent.id)
                } catch (_: IllegalArgumentException) {
                    return@get call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid carId format",
                        "INVALID_CAR_ID",
                        call.callId
                    )
                }
                
                val photoId = try {
                    Uuid.parse(resource.photoId)
                } catch (_: IllegalArgumentException) {
                    return@get call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid photoId format",
                        "INVALID_PHOTO_ID",
                        call.callId
                    )
                }

                val photo = photoService.getById(photoId)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Photo not found")

                if (photo.carId != carId) {
                    throw ApiException(HttpStatusCode.NotFound, message = "Photo does not belong to this car")
                }

                call.respondSuccess(photo.toDto())
            }

            // POST /api/v1/cars/{id}/photos - Create photo metadata
            post<ApiV1.Cars.Id.Photos> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                val userId = Uuid.parse(principal.payload.getClaim("userId").asString())
                val role = Role.valueOf(principal.payload.getClaim("role").asString())

                val carId = try {
                    Uuid.parse(resource.parent.id)
                } catch (_: IllegalArgumentException) {
                    return@post call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid carId format",
                        "INVALID_CAR_ID",
                        call.callId
                    )
                }
                
                val car = carService.getById(carId)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Car not found")

                // Ownership check
                verifyOwnership(role, userId, car.ownerId, "car")

                val req = call.requireBodyOrFail<CreateCarPhotoRequestDto>()
                val created = photoService.create(req.toEntity(carId))
                call.respondCreated(created.toDto())
            }

            // POST /api/v1/cars/{id}/photos/upload - Upload image file
            post<ApiV1.Cars.Id.Photos.Upload> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                val userId = Uuid.parse(principal.payload.getClaim("userId").asString())
                val role = Role.valueOf(principal.payload.getClaim("role").asString())

                val carId = try {
                    Uuid.parse(resource.parent.parent.id)
                } catch (_: IllegalArgumentException) {
                    return@post call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid carId format",
                        "INVALID_CAR_ID",
                        call.callId
                    )
                }
                
                val car = carService.getById(carId)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Car not found")

                // Ownership check
                verifyOwnership(role, userId, car.ownerId, "car")

                // Handle multipart file upload
                val multipart = call.receiveMultipart()
                var fileBytes: ByteArray? = null
                var fileName: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            fileName = part.originalFileName
                            fileBytes = part.provider().toByteArray()
                        }
                        else -> part.dispose()
                    }
                }

                if (fileBytes == null || fileName == null) {
                    throw ApiException(HttpStatusCode.BadRequest, message = "No file provided")
                }

                // Validate file type
                val extension = fileName!!.substringAfterLast('.', "").lowercase()
                if (extension !in listOf("jpg", "jpeg", "png", "webp", "heic")) {
                    throw ApiException(
                        HttpStatusCode.BadRequest,
                        message = "Invalid file type. Allowed: jpg, jpeg, png, webp, heic"
                    )
                }

                // Validate file size (10MB max)
                if (fileBytes!!.size > 10 * 1024 * 1024) {
                    throw ApiException(HttpStatusCode.BadRequest, message = "File too large. Max size: 10MB")
                }

                // Save file
                val uploadDir = File("uploads/car-photos")
                uploadDir.mkdirs()

                val newFileName = "${Uuid.random()}.$extension"
                val file = File(uploadDir, newFileName)
                file.writeBytes(fileBytes!!)

                // Create photo entity
                val photoUrl =
                    "${call.request.origin.scheme}://${call.request.host()}:${call.request.port()}/uploads/car-photos/$newFileName"
                val photo = photoService.create(
                    CarPhoto(
                        id = Uuid.random(),
                        carId = carId,
                        url = photoUrl
                    )
                )

                call.respondCreated(photo.toDto())
            }

            // PATCH /api/v1/cars/{id}/photos/{photoId} - Update photo metadata
            patch<ApiV1.Cars.Id.Photos.PhotoId> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                val userId = Uuid.parse(principal.payload.getClaim("userId").asString())
                val role = Role.valueOf(principal.payload.getClaim("role").asString())

                val carId = try {
                    Uuid.parse(resource.parent.parent.id)
                } catch (_: IllegalArgumentException) {
                    return@patch call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid carId format",
                        "INVALID_CAR_ID",
                        call.callId
                    )
                }
                
                val photoId = try {
                    Uuid.parse(resource.photoId)
                } catch (_: IllegalArgumentException) {
                    return@patch call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid photoId format",
                        "INVALID_PHOTO_ID",
                        call.callId
                    )
                }

                val photo = photoService.getById(photoId)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Photo not found")

                if (photo.carId != carId) {
                    throw ApiException(HttpStatusCode.NotFound, message = "Photo does not belong to this car")
                }

                val car = carService.getById(carId)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Car not found")

                // Ownership check
                verifyOwnership(role, userId, car.ownerId, "car")

                val req = call.requireBodyOrFail<PatchCarPhotoRequestDto>()
                val patched = req.applyPatch(photo)
                val saved = photoService.update(photoId, patched)
                    ?: throw ApiException(
                        HttpStatusCode.InternalServerError,
                        message = "Failed to update photo"
                    )

                call.respondSuccess(saved.toDto())
            }

            // DELETE /api/v1/cars/{id}/photos/{photoId} - Delete photo
            delete<ApiV1.Cars.Id.Photos.PhotoId> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                val userId = Uuid.parse(principal.payload.getClaim("userId").asString())
                val role = Role.valueOf(principal.payload.getClaim("role").asString())

                val carId = try {
                    Uuid.parse(resource.parent.parent.id)
                } catch (_: IllegalArgumentException) {
                    return@delete call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid carId format",
                        "INVALID_CAR_ID",
                        call.callId
                    )
                }
                
                val photoId = try {
                    Uuid.parse(resource.photoId)
                } catch (_: IllegalArgumentException) {
                    return@delete call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid photoId format",
                        "INVALID_PHOTO_ID",
                        call.callId
                    )
                }

                val photo = photoService.getById(photoId)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Photo not found")

                if (photo.carId != carId) {
                    throw ApiException(HttpStatusCode.NotFound, message = "Photo does not belong to this car")
                }

                val car = carService.getById(carId)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Car not found")

                // Ownership check
                verifyOwnership(role, userId, car.ownerId, "car")

                if (!photoService.delete(photoId)) {
                    throw ApiException(HttpStatusCode.InternalServerError, message = "Failed to delete photo")
                }

                call.respondSuccess(Unit, HttpStatusCode.NoContent)
            }
        }
    }
}
