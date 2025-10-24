package dev.koenv.rentmycar.routes.api.v1.cars

import dev.koenv.rentmycar.domain.enums.Role
import dev.koenv.rentmycar.domain.service.CarPhotoService
import dev.koenv.rentmycar.dto.car.CreateCarPhotoRequestDto
import dev.koenv.rentmycar.dto.car.PatchCarPhotoRequestDto
import dev.koenv.rentmycar.mappers.car.applyPatch
import dev.koenv.rentmycar.mappers.car.toDto
import dev.koenv.rentmycar.mappers.car.toEntity
import dev.koenv.rentmycar.routes.RouteRegistrar
import dev.koenv.rentmycar.shared.util.requireBodyOrFail
import dev.koenv.rentmycar.shared.util.requireRole
import dev.koenv.rentmycar.shared.util.requireUuidParamOrFail
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.util.UUID

object CarPhotoRoutes : RouteRegistrar {
	override fun Route.register() {
		val photoService by inject<CarPhotoService>()

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
						if (photo == null || photo.carId != carId) {
							call.respond(HttpStatusCode.NotFound)
						} else {
							call.respond(photo.toDto())
						}
					}

					post {
						call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
						val carId = call.requireUuidParamOrFail("id")
						val req = call.requireBodyOrFail<CreateCarPhotoRequestDto>()
						val created = photoService.create(req.toEntity(carId))
						call.respond(HttpStatusCode.Created, created.toDto())
					}

					patch("/{photoId}") {
						call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
						val carId = call.requireUuidParamOrFail("id")
						val photoId = call.requireUuidParamOrFail("photoId")
						val existing = photoService.getById(photoId)
						if (existing == null || existing.carId != carId) {
							call.respond(HttpStatusCode.NotFound)
							return@patch
						}
						val req = call.requireBodyOrFail<PatchCarPhotoRequestDto>()
						val patched = req.applyPatch(existing)
						val saved = photoService.update(photoId, patched)
						if (saved == null) call.respond(HttpStatusCode.NotFound) else call.respond(saved.toDto())
					}

					delete("/{photoId}") {
						call.requireRole(Role.ADMIN, Role.DRIVER)
						val carId = call.requireUuidParamOrFail("id")
						val photoId = call.requireUuidParamOrFail("photoId")
						val existing = photoService.getById(photoId)
						if (existing == null || existing.carId != carId) {
							call.respond(HttpStatusCode.NotFound)
							return@delete
						}
						if (photoService.delete(photoId)) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
					}
				}
			}
		}
	}
}



