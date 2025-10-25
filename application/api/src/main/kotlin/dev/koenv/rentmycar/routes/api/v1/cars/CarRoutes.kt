package dev.koenv.rentmycar.routes.api.v1.cars

import dev.koenv.rentmycar.domain.enums.CarCategory
import dev.koenv.rentmycar.domain.enums.FuelType
import dev.koenv.rentmycar.domain.enums.Role
import dev.koenv.rentmycar.domain.service.CarService
import dev.koenv.rentmycar.dto.car.CreateCarRequestDto
import dev.koenv.rentmycar.dto.car.PatchCarRequestDto
import dev.koenv.rentmycar.dto.car.UpdateCarRequestDto
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
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.util.UUID

object CarRoutes : RouteRegistrar {
	override fun Route.register() {
		val carService by inject<CarService>()

		route("/cars") {
			// List cars (public, filters)
			get {
				val ownerId = call.request.queryParameters["ownerId"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
				val category = call.request.queryParameters["category"]?.let { runCatching { CarCategory.valueOf(it.uppercase()) }.getOrNull() }
				val fuelType = call.request.queryParameters["fuelType"]?.let { runCatching { FuelType.valueOf(it.uppercase()) }.getOrNull() }
				val isActive = call.request.queryParameters["isActive"]?.toBooleanStrictOrNull()
				val maxRate = call.request.queryParameters["maxRate"]?.let { runCatching { java.math.BigDecimal(it) }.getOrNull() }

				val items = carService.listFiltered(
					ownerId = ownerId,
					category = category,
					fuelType = fuelType,
					isActive = isActive,
					maxRate = maxRate
				)
				call.respond(items.map { it.toDto() })
			}

			// CRUD: require auth
			authenticate("auth-jwt") {
				get("/{id}") {
					call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
					val id = call.requireUuidParamOrFail("id")
					val car = carService.getById(id)
					if (car == null) call.respond(HttpStatusCode.NotFound) else call.respond(car.toDto())
				}

				post {
					val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
					val ownerId = UUID.fromString(principal.payload.getClaim("userId").asString())
					val req = call.requireBodyOrFail<CreateCarRequestDto>()
					val created = carService.create(req.toEntity(ownerId))
					call.respond(HttpStatusCode.Created, created.toDto())
				}

				put("/{id}") {
					val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
					val ownerId = UUID.fromString(principal.payload.getClaim("userId").asString())
					val id = call.requireUuidParamOrFail("id")
					val req = call.requireBodyOrFail<UpdateCarRequestDto>()
					val updated = carService.update(id, req.toEntity(id, ownerId))
					if (updated == null) call.respond(HttpStatusCode.NotFound) else call.respond(updated.toDto())
				}

				patch("/{id}") {
					call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
					val id = call.requireUuidParamOrFail("id")
					val existing = carService.getById(id)
					if (existing == null) {
						call.respond(HttpStatusCode.NotFound)
						return@patch
					}
					val req = call.requireBodyOrFail<PatchCarRequestDto>()
					val patched = req.applyPatch(existing)
					val saved = carService.update(id, patched)
					if (saved == null) call.respond(HttpStatusCode.NotFound) else call.respond(saved.toDto())
				}

				delete("/{id}") {
					call.requireRole(Role.ADMIN, Role.DRIVER)
					val id = call.requireUuidParamOrFail("id")
					if (carService.delete(id)) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
				}
			}
		}
	}
}


