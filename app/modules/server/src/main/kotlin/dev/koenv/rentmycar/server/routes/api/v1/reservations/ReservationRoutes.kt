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
import dev.koenv.rentmycar.shared.resources.ApiV1
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.resources.patch
import io.ktor.server.resources.delete
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

        // POST /api/v1/reservations/quote - Public quote endpoint
        post<ApiV1.Reservations.Quote> {
            val request = call.requireBodyOrFail<ReservationQuoteRequestDto>()

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

            call.respondSuccess(response)
        }

        authenticate("auth-jwt") {
            // GET /api/v1/reservations - List with filters
            get<ApiV1.Reservations> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                val userId = principal.getUserId()
                val role = principal.getRole()

                val renterId = resource.renterId?.let { runCatching { Uuid.parse(it) }.getOrNull() }
                val carId = resource.carId?.let { runCatching { Uuid.parse(it) }.getOrNull() }
                val status = resource.status?.let {
                    runCatching { ReservationStatus.valueOf(it.uppercase()) }.getOrNull()
                }
                val startDateTime = resource.start?.let { runCatching { kotlinx.datetime.LocalDateTime.parse(it) }.getOrNull() }
                val endDateTime = resource.end?.let { runCatching { kotlinx.datetime.LocalDateTime.parse(it) }.getOrNull() }

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

                call.respondSuccess(filtered.map { it.toDto() })
            }

            // GET /api/v1/reservations/active - Get active reservations
            get<ApiV1.Reservations.Active> {
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

                call.respondSuccess(filtered.map { it.toDto() })
            }

            // GET /api/v1/reservations/my-cars - Get reservations for user's owned cars
            get<ApiV1.Reservations.MyCars> {
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                val userId = principal.getUserId()
                val role = principal.getRole()

                // Get user's owned cars
                val ownedCars = carService.getAll().filter { car ->
                    role == Role.ADMIN || car.ownerId == userId
                }

                // Precompute owned car IDs for efficient lookups
                val ownedCarIds = ownedCars.mapTo(HashSet()) { it.id }

                // Get all reservations for those cars
                val reservations = reservationService.getAll().filter { reservation ->
                    reservation.carId in ownedCarIds
                }

                call.respondSuccess(reservations.map { it.toDto() })
            }

            // GET /api/v1/reservations/{id} - Get specific reservation
            get<ApiV1.Reservations.Id> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                val userId = principal.getUserId()
                val role = principal.getRole()
                
                val id = try {
                    Uuid.parse(resource.id)
                } catch (_: IllegalArgumentException) {
                    return@get call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid reservation ID format",
                        "INVALID_RESERVATION_ID",
                        call.callId
                    )
                }
                
                val reservation = reservationService.getById(id)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

                // Non-admin users can view if they are the renter OR the car owner
                if (role != Role.ADMIN) {
                    val isRenter = reservation.renterId == userId
                    val car = carService.getById(reservation.carId)
                        ?: throw ApiException(
                            HttpStatusCode.NotFound,
                            message = "Car associated with this reservation was not found"
                        )
                    val isCarOwner = car.ownerId == userId
                    if (!isRenter && !isCarOwner) {
                        throw ApiException(
                            HttpStatusCode.Forbidden,
                            message = "You are not authorized to view this reservation"
                        )
                    }
                }

                call.respondSuccess(reservation.toDto())
            }

            // POST /api/v1/reservations - Create reservation
            post<ApiV1.Reservations> {
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                val renterId = principal.getUserId()
                val req = call.requireBodyOrFail<CreateReservationRequestDto>()
                val created = reservationService.create(req.toEntity(renterId))
                call.respondCreated(created.toDto())
            }

            // PUT /api/v1/reservations/{id} - Replace reservation
            put<ApiV1.Reservations.Id> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                val renterId = principal.getUserId()
                val role = principal.getRole()
                
                val id = try {
                    Uuid.parse(resource.id)
                } catch (_: IllegalArgumentException) {
                    return@put call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid reservation ID format",
                        "INVALID_RESERVATION_ID",
                        call.callId
                    )
                }
                
                val existing = reservationService.getById(id)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

                verifyOwnership(role, renterId, existing.renterId, "reservation")

                val req = call.requireBodyOrFail<UpdateReservationRequestDto>()
                val updated = reservationService.update(
                    id,
                    req.toEntity(id, existing.renterId, existing.priceTotal, existing.pointsAwarded)
                )
                if (updated == null) {
                    call.respondError(HttpStatusCode.NotFound, "Reservation not found")
                } else {
                    call.respondSuccess(updated.toDto())
                }
            }

            // PATCH /api/v1/reservations/{id} - Partially update reservation
            patch<ApiV1.Reservations.Id> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                val renterId = principal.getUserId()
                val role = principal.getRole()
                
                val id = try {
                    Uuid.parse(resource.id)
                } catch (_: IllegalArgumentException) {
                    return@patch call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid reservation ID format",
                        "INVALID_RESERVATION_ID",
                        call.callId
                    )
                }
                
                val existing = reservationService.getById(id)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

                verifyOwnership(role, renterId, existing.renterId, "reservation")

                val req = call.requireBodyOrFail<PatchReservationRequestDto>()
                val patched = req.applyPatch(existing)
                val saved = reservationService.update(id, patched)
                if (saved == null) {
                    call.respondError(HttpStatusCode.NotFound, "Reservation not found")
                } else {
                    call.respondSuccess(saved.toDto())
                }
            }

            // DELETE /api/v1/reservations/{id} - Delete reservation
            delete<ApiV1.Reservations.Id> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                val renterId = principal.getUserId()
                val role = principal.getRole()
                
                val id = try {
                    Uuid.parse(resource.id)
                } catch (_: IllegalArgumentException) {
                    return@delete call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid reservation ID format",
                        "INVALID_RESERVATION_ID",
                        call.callId
                    )
                }
                
                val existing = reservationService.getById(id)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

                verifyOwnership(role, renterId, existing.renterId, "reservation")

                if (reservationService.delete(id)) {
                    call.respondSuccess(Unit)
                } else {
                    call.respondError(HttpStatusCode.NotFound, "Reservation not found")
                }
            }

            // POST /api/v1/reservations/{id}/cancel - Cancel reservation
            post<ApiV1.Reservations.Id.Cancel> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                val userId = principal.getUserId()
                val role = principal.getRole()
                
                val id = try {
                    Uuid.parse(resource.parent.id)
                } catch (_: IllegalArgumentException) {
                    return@post call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid reservation ID format",
                        "INVALID_RESERVATION_ID",
                        call.callId
                    )
                }
                
                val existing = reservationService.getById(id)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

                // Only renter or car owner (or admin) can cancel
                if (role != Role.ADMIN) {
                    val isRenter = existing.renterId == userId
                    val car = carService.getById(existing.carId)
                    val isCarOwner = car?.ownerId == userId
                    
                    if (!isRenter && !isCarOwner) {
                        throw ApiException(
                            HttpStatusCode.Forbidden,
                            message = "Only the renter or car owner can cancel this reservation"
                        )
                    }
                }

                if (reservationService.cancel(id)) {
                    call.respondSuccess(Unit)
                } else {
                    call.respondError(HttpStatusCode.NotFound, "Reservation not found")
                }
            }

            // POST /api/v1/reservations/{id}/confirm - Confirm reservation
            post<ApiV1.Reservations.Id.Confirm> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                val userId = principal.getUserId()
                val role = principal.getRole()
                
                val id = try {
                    Uuid.parse(resource.parent.id)
                } catch (_: IllegalArgumentException) {
                    return@post call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid reservation ID format",
                        "INVALID_RESERVATION_ID",
                        call.callId
                    )
                }

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
                call.respondSuccess(confirmed.toDto())
            }

            // POST /api/v1/reservations/{id}/complete - Complete reservation
            post<ApiV1.Reservations.Id.Complete> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                val userId = principal.getUserId()
                val role = principal.getRole()
                
                val id = try {
                    Uuid.parse(resource.parent.id)
                } catch (_: IllegalArgumentException) {
                    return@post call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid reservation ID format",
                        "INVALID_RESERVATION_ID",
                        call.callId
                    )
                }

                val reservation = reservationService.getById(id)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

                // Only renter or admin can complete
                verifyOwnership(role, userId, reservation.renterId, "reservation")

                val completed = reservationService.completeReservation(id)
                call.respondSuccess(completed.toDto())
            }

            // GET /api/v1/reservations/{id}/driving-sessions - Get driving sessions
            get<ApiV1.Reservations.Id.DrivingSessions> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                val userId = principal.getUserId()
                val role = principal.getRole()
                
                val id = try {
                    Uuid.parse(resource.parent.id)
                } catch (_: IllegalArgumentException) {
                    return@get call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid reservation ID format",
                        "INVALID_RESERVATION_ID",
                        call.callId
                    )
                }

                val reservation = reservationService.getById(id)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

                // Non-admin users can view if they are the renter OR the car owner
                if (role != Role.ADMIN) {
                    val isRenter = reservation.renterId == userId
                    val car = carService.getById(reservation.carId)
                    val isCarOwner = car?.ownerId == userId
                    
                    if (!isRenter && !isCarOwner) {
                        throw ApiException(
                            HttpStatusCode.Forbidden,
                            message = "You can only view driving sessions for your own reservations or cars"
                        )
                    }
                }

                val sessions = drivingSessionService.getByReservationId(id)
                call.respondSuccess(sessions.map { it.toDto(drivingSessionService) })
            }

            // POST /api/v1/reservations/{id}/driving-sessions - Create driving session
            post<ApiV1.Reservations.Id.DrivingSessions> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                val userId = principal.getUserId()
                
                val reservationId = try {
                    Uuid.parse(resource.parent.id)
                } catch (_: IllegalArgumentException) {
                    return@post call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid reservation ID format",
                        "INVALID_RESERVATION_ID",
                        call.callId
                    )
                }
                
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

                call.respondCreated(created.toDto(drivingSessionService))
            }
        }
    }
}
