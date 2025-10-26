package dev.koenv.rentmycar.routes.api.v1.reservations

import dev.koenv.rentmycar.domain.enums.ReservationStatus
import dev.koenv.rentmycar.domain.enums.Role
import dev.koenv.rentmycar.domain.service.DrivingSessionService
import dev.koenv.rentmycar.domain.service.ReservationService
import dev.koenv.rentmycar.dto.reservation.CreateReservationRequestDto
import dev.koenv.rentmycar.dto.reservation.PatchReservationRequestDto
import dev.koenv.rentmycar.dto.reservation.UpdateReservationRequestDto
import dev.koenv.rentmycar.mappers.reservation.applyPatch
import dev.koenv.rentmycar.mappers.reservation.toDto
import dev.koenv.rentmycar.mappers.reservation.toEntity
import dev.koenv.rentmycar.routes.RouteRegistrar
import dev.koenv.rentmycar.shared.util.requireBodyOrFail
import dev.koenv.rentmycar.shared.util.requireRole
import dev.koenv.rentmycar.shared.util.requireUuidParamOrFail
import dev.koenv.rentmycar.shared.util.requireUuidParamOrNull
import dev.koenv.rentmycar.shared.util.requireLongParamOrNull
import dev.koenv.rentmycar.shared.http.ApiException
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
import kotlinx.datetime.Clock
import kotlinx.datetime.toLocalDateTime
import org.koin.ktor.ext.inject
import java.util.UUID

object ReservationRoutes : RouteRegistrar {
	override fun Route.register() {
		val reservationService by inject<ReservationService>()
		val drivingSessionService by inject<DrivingSessionService>()

		route("/reservations") {
			authenticate("auth-jwt") {
				// List with filters
				get {
					call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
						val renterId = call.requireUuidParamOrNull("renterId")
						val carId = call.requireUuidParamOrNull("carId")
						val status = call.request.queryParameters["status"]?.uppercase()?.let {
							try { ReservationStatus.valueOf(it) } catch (_: IllegalArgumentException) {
								throw ApiException(io.ktor.http.HttpStatusCode.BadRequest, message = "Invalid status value")
							}
						}
						val startMillis = call.requireLongParamOrNull("start")
						val endMillis = call.requireLongParamOrNull("end")
					val all = reservationService.getAll()
					val filtered = all.asSequence()
						.filter { renterId == null || it.renterId == renterId }
						.filter { carId == null || it.carId == carId }
						.filter { status == null || it.status == status }
							.filter { startMillis == null || it.endTime >= kotlinx.datetime.Instant.fromEpochMilliseconds(startMillis).toLocalDateTime(kotlinx.datetime.TimeZone.UTC) }
							.filter { endMillis == null || it.startTime <= kotlinx.datetime.Instant.fromEpochMilliseconds(endMillis).toLocalDateTime(kotlinx.datetime.TimeZone.UTC) }
						.toList()
					call.respond(filtered.map { it.toDto() })
				}

				get("/{id}") {
					call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
					val id = call.requireUuidParamOrFail("id")
					val reservation = reservationService.getById(id)
					if (reservation == null) call.respond(HttpStatusCode.NotFound) else call.respond(reservation.toDto())
				}

				post {
					val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
					val renterId = UUID.fromString(principal.payload.getClaim("userId").asString())
					val req = call.requireBodyOrFail<CreateReservationRequestDto>()
					val created = reservationService.create(req.toEntity(renterId))
					call.respond(HttpStatusCode.Created, created.toDto())
				}

				put("/{id}") {
					val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
					val renterId = UUID.fromString(principal.payload.getClaim("userId").asString())
					val id = call.requireUuidParamOrFail("id")
					val req = call.requireBodyOrFail<UpdateReservationRequestDto>()
					val updated = reservationService.update(id, req.toEntity(id, renterId))
					if (updated == null) call.respond(HttpStatusCode.NotFound) else call.respond(updated.toDto())
				}

				patch("/{id}") {
					call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
					val id = call.requireUuidParamOrFail("id")
					val existing = reservationService.getById(id)
					if (existing == null) {
						call.respond(HttpStatusCode.NotFound)
						return@patch
					}
					val req = call.requireBodyOrFail<PatchReservationRequestDto>()
					val patched = req.applyPatch(existing)
					val saved = reservationService.update(id, patched)
					if (saved == null) call.respond(HttpStatusCode.NotFound) else call.respond(saved.toDto())
				}

				delete("/{id}") {
					call.requireRole(Role.ADMIN, Role.DRIVER)
					val id = call.requireUuidParamOrFail("id")
					if (reservationService.delete(id)) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
				}

				post("/{id}/cancel") {
					call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
					val id = call.requireUuidParamOrFail("id")
					val ok = reservationService.cancel(id)
					if (ok) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
				}

				get("/active") {
					call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
					val now = Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.UTC)
					val active = reservationService.findActiveReservations(now)
					call.respond(active.map { it.toDto() })
				}

				get("/{id}/driving-sessions") {
					call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
					val id = call.requireUuidParamOrFail("id")
					val sessions = drivingSessionService.getByReservationId(id)
					call.respond(sessions)
				}
			}
		}
	}
}


