package dev.koenv.rentmycar.routes.api.v1.cars

import dev.koenv.rentmycar.domain.enums.Role
import dev.koenv.rentmycar.domain.service.CarPhotoService
import dev.koenv.rentmycar.domain.service.CarService
import dev.koenv.rentmycar.dto.car.CreateCarPhotoRequestDto
import dev.koenv.rentmycar.dto.car.PatchCarPhotoRequestDto
import dev.koenv.rentmycar.mappers.car.applyPatch
import dev.koenv.rentmycar.mappers.car.toDto
import dev.koenv.rentmycar.mappers.car.toEntity
import dev.koenv.rentmycar.routes.RouteRegistrar
import dev.koenv.rentmycar.shared.http.ApiException
import dev.koenv.rentmycar.shared.util.requireBodyOrFail
import dev.koenv.rentmycar.shared.util.requireRole
import dev.koenv.rentmycar.shared.util.requireUuidParamOrFail
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

object CarPhotoRoutes : RouteRegistrar {
    override fun Route.register() {
        val photoService by inject<CarPhotoService>()
        val carService by inject<CarService>()

        route("/cars") {
            authenticate("auth-jwt") {
                route("/{id}/photos") {

                    get {
                        call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                        val carId = call.requireUuidParamOrFail("id")
                        val items = photoService.getByCarId(carId)
                        call.respond(items.map { it.toDto() })
                    }

                    get("/{photoId}") {
                        call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                        val carId = call.requireUuidParamOrFail("id")
                        val photoId = call.requireUuidParamOrFail("photoId")

                        val photo = photoService.getById(photoId)
                            ?: throw ApiException(HttpStatusCode.NotFound, message = "Photo not found")

                        if (photo.carId != carId) {
                            throw ApiException(HttpStatusCode.NotFound, message = "Photo does not belong to this car")
                        }

                        call.respond(photo.toDto())
                    }

                    post {
                        val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                        val userId = UUID.fromString(principal.payload.getClaim("userId").asString())
                        val role = Role.valueOf(principal.payload.getClaim("role").asString())

                        val carId = call.requireUuidParamOrFail("id")
                        val car = carService.getById(carId)
                            ?: throw ApiException(HttpStatusCode.NotFound, message = "Car not found")

                        // Ownership check
                        if (role != Role.ADMIN && car.ownerId != userId) {
                            throw ApiException(HttpStatusCode.Forbidden, message = "You are not the owner of this car")
                        }

                        val req = call.requireBodyOrFail<CreateCarPhotoRequestDto>()
                        val created = photoService.create(req.toEntity(carId))
                        call.respond(HttpStatusCode.Created, created.toDto())
                    }

                    patch("/{photoId}") {
                        val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                        val userId = UUID.fromString(principal.payload.getClaim("userId").asString())
                        val role = Role.valueOf(principal.payload.getClaim("role").asString())

                        val carId = call.requireUuidParamOrFail("id")
                        val photoId = call.requireUuidParamOrFail("photoId")

                        val photo = photoService.getById(photoId)
                            ?: throw ApiException(HttpStatusCode.NotFound, message = "Photo not found")

                        if (photo.carId != carId) {
                            throw ApiException(HttpStatusCode.NotFound, message = "Photo does not belong to this car")
                        }

                        val car = carService.getById(carId)
                            ?: throw ApiException(HttpStatusCode.NotFound, message = "Car not found")

                        // Ownership check
                        if (role != Role.ADMIN && car.ownerId != userId) {
                            throw ApiException(HttpStatusCode.Forbidden, message = "You are not the owner of this car")
                        }

                        val req = call.requireBodyOrFail<PatchCarPhotoRequestDto>()
                        val patched = req.applyPatch(photo)
                        val saved = photoService.update(photoId, patched)
                            ?: throw ApiException(HttpStatusCode.InternalServerError, message = "Failed to update photo")

                        call.respond(saved.toDto())
                    }

                    delete("/{photoId}") {
                        val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                        val userId = UUID.fromString(principal.payload.getClaim("userId").asString())
                        val role = Role.valueOf(principal.payload.getClaim("role").asString())

                        val carId = call.requireUuidParamOrFail("id")
                        val photoId = call.requireUuidParamOrFail("photoId")

                        val photo = photoService.getById(photoId)
                            ?: throw ApiException(HttpStatusCode.NotFound, message = "Photo not found")

                        if (photo.carId != carId) {
                            throw ApiException(HttpStatusCode.NotFound, message = "Photo does not belong to this car")
                        }

                        val car = carService.getById(carId)
                            ?: throw ApiException(HttpStatusCode.NotFound, message = "Car not found")

                        // Ownership check
                        if (role != Role.ADMIN && car.ownerId != userId) {
                            throw ApiException(HttpStatusCode.Forbidden, message = "You are not the owner of this car")
                        }

                        if (!photoService.delete(photoId)) {
                            throw ApiException(HttpStatusCode.InternalServerError, message = "Failed to delete photo")
                        }

                        call.respond(HttpStatusCode.NoContent)
                    }
                }
            }
        }
    }
}
