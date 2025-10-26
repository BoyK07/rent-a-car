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
import dev.koenv.rentmycar.shared.http.ApiException
import dev.koenv.rentmycar.shared.util.*
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
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
                    val renterId = call.request.queryParameters["renterId"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                    val carId = call.request.queryParameters["carId"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                    val status = call.request.queryParameters["status"]?.let {
                        runCatching { ReservationStatus.valueOf(it.uppercase()) }.getOrNull()
                    }
                    val startMillis = call.request.queryParameters["start"]?.toLongOrNull()
                    val endMillis = call.request.queryParameters["end"]?.toLongOrNull()

                    val all = reservationService.getAll()
                    val filtered = all.asSequence()
                        .filter { r -> renterId == null || r.renterId == renterId }
                        .filter { r -> carId == null || r.carId == carId }
                        .filter { r -> status == null || r.status == status }
                        .filter { r ->
                            startMillis == null || r.endTime >= Instant.fromEpochMilliseconds(startMillis)
                                .toLocalDateTime(kotlinx.datetime.TimeZone.UTC)
                        }
                        .filter { r ->
                            endMillis == null || r.startTime <= Instant.fromEpochMilliseconds(endMillis)
                                .toLocalDateTime(kotlinx.datetime.TimeZone.UTC)
                        }
                        .toList()

                    call.respond(filtered.map { it.toDto() })
                }

                get("/{id}") {
                    call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                    val id = call.requireUuidParamOrFail("id")
                    val reservation = reservationService.getById(id)
                        ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")
                    call.respond(reservation.toDto())
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
                    val role = Role.valueOf(principal.payload.getClaim("role").asString())
                    val id = call.requireUuidParamOrFail("id")
                    val existing = reservationService.getById(id)
                        ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

                    if (role != Role.ADMIN && existing.renterId != renterId) {
                        throw ApiException(
                            HttpStatusCode.Forbidden,
                            message = "You are not the renter of this reservation"
                        )
                    }

                    val req = call.requireBodyOrFail<UpdateReservationRequestDto>()
                    val updated = reservationService.update(id, req.toEntity(id, existing.renterId))
                    if (updated == null) call.respond(HttpStatusCode.NotFound) else call.respond(updated.toDto())
                }

                patch("/{id}") {
                    val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                    val renterId = UUID.fromString(principal.payload.getClaim("userId").asString())
                    val role = Role.valueOf(principal.payload.getClaim("role").asString())
                    val id = call.requireUuidParamOrFail("id")
                    val existing = reservationService.getById(id)
                        ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

                    if (role != Role.ADMIN && existing.renterId != renterId) {
                        throw ApiException(
                            HttpStatusCode.Forbidden,
                            message = "You are not the renter of this reservation"
                        )
                    }

                    val req = call.requireBodyOrFail<PatchReservationRequestDto>()
                    val patched = req.applyPatch(existing)
                    val saved = reservationService.update(id, patched)
                    if (saved == null) call.respond(HttpStatusCode.NotFound) else call.respond(saved.toDto())
                }

                delete("/{id}") {
                    val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                    val renterId = UUID.fromString(principal.payload.getClaim("userId").asString())
                    val role = Role.valueOf(principal.payload.getClaim("role").asString())
                    val id = call.requireUuidParamOrFail("id")
                    val existing = reservationService.getById(id)
                        ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

                    if (role != Role.ADMIN && existing.renterId != renterId) {
                        throw ApiException(
                            HttpStatusCode.Forbidden,
                            message = "You are not the renter of this reservation"
                        )
                    }

                    if (reservationService.delete(id)) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                post("/{id}/cancel") {
                    val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                    val renterId = UUID.fromString(principal.payload.getClaim("userId").asString())
                    val role = Role.valueOf(principal.payload.getClaim("role").asString())
                    val id = call.requireUuidParamOrFail("id")
                    val existing = reservationService.getById(id)
                        ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

                    if (role != Role.ADMIN && existing.renterId != renterId) {
                        throw ApiException(
                            HttpStatusCode.Forbidden,
                            message = "You are not the renter of this reservation"
                        )
                    }

                    if (reservationService.cancel(id)) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
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
