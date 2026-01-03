package dev.koenv.rentmycar.server.routes.api.v1.reservations

import dev.koenv.rentmycar.server.domain.service.CarService
import dev.koenv.rentmycar.server.domain.service.DrivingSessionService
import dev.koenv.rentmycar.server.domain.service.ReservationService
import dev.koenv.rentmycar.server.mappers.drivingsession.toDto
import dev.koenv.rentmycar.server.mappers.drivingsession.toEntity
import dev.koenv.rentmycar.server.mappers.reservation.applyPatch
import dev.koenv.rentmycar.server.mappers.reservation.toDto
import dev.koenv.rentmycar.server.mappers.reservation.toEntity
import dev.koenv.rentmycar.server.routes.RouteRegistrar
import dev.koenv.rentmycar.server.util.*
import dev.koenv.rentmycar.shared.domain.enums.ReservationStatus
import dev.koenv.rentmycar.shared.domain.enums.Role
import dev.koenv.rentmycar.shared.dto.reservation.CreateDrivingSessionRequestDto
import dev.koenv.rentmycar.shared.dto.reservation.CreateReservationRequestDto
import dev.koenv.rentmycar.shared.dto.reservation.PatchReservationRequestDto
import dev.koenv.rentmycar.shared.dto.reservation.ReservationQuoteRequestDto
import dev.koenv.rentmycar.shared.dto.reservation.ReservationQuoteResponseDto
import dev.koenv.rentmycar.shared.dto.reservation.UpdateReservationRequestDto
import dev.koenv.rentmycar.shared.http.ApiException
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.ktor.ext.inject
import kotlin.time.Clock
import kotlin.uuid.Uuid

object ReservationRoutes : RouteRegistrar {
    override fun Route.register() {
        val reservationService by inject<ReservationService>()
        val drivingSessionService by inject<DrivingSessionService>()
        val carService by inject<CarService>()

        route("/reservations") {

            // Public quote endpoint - get price estimate before booking
            post("/quote") {
                val request =
                    call.requireBodyOrFail<ReservationQuoteRequestDto>()

                val (price, duration, car) = reservationService.getQuote(
                    request.carId,
                    request.startTime,
                    request.endTime
                )

                val carId = car.id
                require(carId != null) { "Car ID must not be null" }

                val response = ReservationQuoteResponseDto(
                    carId = carId,
                    carBrand = car.brand,
                    carModel = car.model,
                    startTime = request.startTime,
                    endTime = request.endTime,
                    durationHours = duration,
                    ratePerHour = car.ratePerHour,
                    totalPrice = price
                )

                call.respond(HttpStatusCode.OK, response)
            }

            authenticate("auth-jwt") {
                // List with filters
                get {
                    val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                    val userId = principal.getUserId()
                    val role = principal.getRole()

                    val renterId =
                        call.request.queryParameters["renterId"]?.let { runCatching { Uuid.parse(it) }.getOrNull() }
                    val carId =
                        call.request.queryParameters["carId"]?.let { runCatching { Uuid.parse(it) }.getOrNull() }
                    val status = call.request.queryParameters["status"]?.let {
                        runCatching { ReservationStatus.valueOf(it.uppercase()) }.getOrNull()
                    }
                    val startDateTime = call.requireLocalDateTimeParamOrNull("start")
                    val endDateTime = call.requireLocalDateTimeParamOrNull("end")

                    val all = reservationService.getAll()
                    val filtered = all.asSequence()
                        .filter { r -> renterId == null || r.renterId == renterId }
                        .filter { r -> carId == null || r.carId == carId }
                        .filter { r -> status == null || r.status == status }
                        .filter { r ->
                            startDateTime == null || r.endTime >= startDateTime
                        }
                        .filter { r ->
                            endDateTime == null || r.startTime <= endDateTime
                        }
                        // Non-admin users can only see their own reservations
                        .filter { r -> role == Role.ADMIN || r.renterId == userId }
                        .toList()

                    call.respond(filtered.map { it.toDto() })
                }

                get("/{id}") {
                    val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                    val userId = principal.getUserId()
                    val role = principal.getRole()
                    val id = call.requireUuidParamOrFail("id")
                    val reservation = reservationService.getById(id)
                        ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

                    // Non-admin users can only view their own reservations
                    if (role != Role.ADMIN && reservation.renterId != userId) {
                        throw ApiException(
                            HttpStatusCode.Forbidden,
                            message = "You are not authorized to view this reservation"
                        )
                    }

                    call.respond(reservation.toDto())
                }

                post {
                    val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                    val renterId = principal.getUserId()
                    val req = call.requireBodyOrFail<CreateReservationRequestDto>()
                    val created = reservationService.create(req.toEntity(renterId))
                    call.respond(HttpStatusCode.Created, created.toDto())
                }

                put("/{id}") {
                    val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                    val renterId = principal.getUserId()
                    val role = principal.getRole()
                    val id = call.requireUuidParamOrFail("id")
                    val existing = reservationService.getById(id)
                        ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

                    verifyOwnership(role, renterId, existing.renterId, "reservation")

                    val req = call.requireBodyOrFail<UpdateReservationRequestDto>()
                    val updated = reservationService.update(
                        id,
                        req.toEntity(id, existing.renterId, existing.priceTotal, existing.pointsAwarded)
                    )
                    if (updated == null) call.respond(HttpStatusCode.NotFound) else call.respond(updated.toDto())
                }

                patch("/{id}") {
                    val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                    val renterId = principal.getUserId()
                    val role = principal.getRole()
                    val id = call.requireUuidParamOrFail("id")
                    val existing = reservationService.getById(id)
                        ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

                    verifyOwnership(role, renterId, existing.renterId, "reservation")

                    val req = call.requireBodyOrFail<PatchReservationRequestDto>()
                    val patched = req.applyPatch(existing)
                    val saved = reservationService.update(id, patched)
                    if (saved == null) call.respond(HttpStatusCode.NotFound) else call.respond(saved.toDto())
                }

                delete("/{id}") {
                    val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                    val renterId = principal.getUserId()
                    val role = principal.getRole()
                    val id = call.requireUuidParamOrFail("id")
                    val existing = reservationService.getById(id)
                        ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

                    verifyOwnership(role, renterId, existing.renterId, "reservation")

                    if (reservationService.delete(id)) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                post("/{id}/cancel") {
                    val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                    val renterId = principal.getUserId()
                    val role = principal.getRole()
                    val id = call.requireUuidParamOrFail("id")
                    val existing = reservationService.getById(id)
                        ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

                    verifyOwnership(role, renterId, existing.renterId, "reservation")

                    if (reservationService.cancel(id)) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                post("/{id}/confirm") {
                    val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                    val userId = principal.getUserId()
                    val role = principal.getRole()
                    val id = call.requireUuidParamOrFail("id")

                    val reservation = reservationService.getById(id)
                        ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

                    // Only car owner or admin can confirm
                    val car = carService.getById(reservation.carId)
                        ?: throw ApiException(HttpStatusCode.NotFound, message = "Car not found")
                    if (role != Role.ADMIN && car.ownerId != userId) {
                        throw ApiException(
                            HttpStatusCode.Forbidden,
                            message = "Only car owner can confirm reservations"
                        )
                    }

                    val confirmed = reservationService.confirmReservation(id)
                    call.respond(confirmed.toDto())
                }

                post("/{id}/complete") {
                    val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                    val userId = principal.getUserId()
                    val role = principal.getRole()
                    val id = call.requireUuidParamOrFail("id")

                    val reservation = reservationService.getById(id)
                        ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

                    // Only renter or admin can complete
                    verifyOwnership(role, userId, reservation.renterId, "reservation")

                    val completed = reservationService.completeReservation(id)
                    call.respond(completed.toDto())
                }

                get("/active") {
                    val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                    val userId = principal.getUserId()
                    val role = principal.getRole()
                    val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                    val active = reservationService.findActiveReservations(now)

                    // Non-admin users can only see their own active reservations
                    val filtered = if (role == Role.ADMIN) {
                        active
                    } else {
                        active.filter { it.renterId == userId }
                    }

                    call.respond(filtered.map { it.toDto() })
                }

                get("/{id}/driving-sessions") {
                    val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                    val userId = principal.getUserId()
                    val role = principal.getRole()
                    val id = call.requireUuidParamOrFail("id")

                    val reservation = reservationService.getById(id)
                        ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

                    // Non-admin users can only view driving sessions for their own reservations
                    if (role != Role.ADMIN && reservation.renterId != userId) {
                        throw ApiException(
                            HttpStatusCode.Forbidden,
                            message = "You can only view driving sessions for your own reservations"
                        )
                    }

                    val sessions = drivingSessionService.getByReservationId(id)
                    call.respond(sessions.map { it.toDto(drivingSessionService) })
                }

                post("/{id}/driving-sessions") {
                    val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                    val userId = principal.getUserId()
                    val reservationId = call.requireUuidParamOrFail("id")
                    val request = call.requireBodyOrFail<CreateDrivingSessionRequestDto>()

                    // Validate reservation exists and user is the renter
                    val reservation = reservationService.getById(reservationId)
                        ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

                    // Only the renter can create sessions for their reservation
                    if (reservation.renterId != userId) {
                        throw ApiException(
                            HttpStatusCode.Forbidden,
                            message = "You can only create sessions for your own reservations"
                        )
                    }

                    val session = request.toEntity(reservationId, userId)
                    val created = drivingSessionService.create(session)

                    call.respond(HttpStatusCode.Created, created.toDto(drivingSessionService))
                }
            }
        }
    }
}
