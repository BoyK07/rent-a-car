package dev.koenv.rentmycar.server.domain.service

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.koenv.rentmycar.server.storage.repository.CarRepositoryImpl
import dev.koenv.rentmycar.server.storage.repository.ReservationRepositoryImpl
import dev.koenv.rentmycar.server.storage.repository.UserRepositoryImpl
import dev.koenv.rentmycar.shared.domain.entity.Car
import dev.koenv.rentmycar.shared.domain.entity.Reservation
import dev.koenv.rentmycar.shared.domain.enums.ReservationStatus
import dev.koenv.rentmycar.shared.http.ApiException
import io.ktor.http.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.uuid.Uuid

class ReservationService(
    private val repo: ReservationRepositoryImpl,
    private val carRepository: CarRepositoryImpl,
    private val userRepository: UserRepositoryImpl,
) {
    suspend fun getAll(): List<Reservation> = repo.findAll()
    suspend fun getById(id: Uuid): Reservation? = repo.findById(id)

    /**
     * Creates a new reservation with comprehensive validation and server-side price calculation.
     */
    suspend fun create(reservation: Reservation): Reservation {
        validateForeignKeys(reservation)
        validateBusinessRules(reservation)

        // Check for conflicting reservations
        if (hasConflictingReservations(reservation.carId, reservation.startTime, reservation.endTime)) {
            throw ApiException(
                http = HttpStatusCode.Conflict,
                code = "RESERVATION_CONFLICT",
                message = "This time slot is already reserved"
            )
        }

        // Calculate price server-side
        val car = carRepository.findById(reservation.carId)
            ?: throw ApiException(
                HttpStatusCode.NotFound,
                code = "CAR_NOT_FOUND",
                message = "Car not found"
            )
        val calculatedPrice = calculatePrice(car.ratePerHour, reservation.startTime, reservation.endTime)

        // Override client-provided price with server calculation
        val validatedReservation = reservation.copy(
            priceTotal = calculatedPrice,
            status = ReservationStatus.PENDING
        )

        return repo.create(validatedReservation)
    }

    suspend fun update(id: Uuid, reservation: Reservation): Reservation? {
        validateForeignKeys(reservation)
        validateBusinessRules(reservation)

        // Check for conflicts, excluding current reservation
        val existingReservations = repo.findByCarId(reservation.carId)
        val hasConflict = existingReservations.any { existing ->
            existing.id != id &&
                    existing.status != ReservationStatus.CANCELLED &&
                    existing.status != ReservationStatus.COMPLETED &&
                    !(reservation.endTime <= existing.startTime || reservation.startTime >= existing.endTime)
        }

        if (hasConflict) {
            throw ApiException(
                http = HttpStatusCode.Conflict,
                code = "RESERVATION_CONFLICT",
                message = "This time slot is already reserved"
            )
        }

        return repo.update(id, reservation)
    }

    suspend fun delete(id: Uuid): Boolean = repo.delete(id)

    /**
     * Cancels a reservation. Only allowed for PENDING or CONFIRMED reservations.
     */
    suspend fun cancel(id: Uuid): Boolean {
        val reservation = repo.findById(id) ?: return false

        if (reservation.status == ReservationStatus.COMPLETED || reservation.status == ReservationStatus.CANCELLED) {
            return false
        }

        val updated = reservation.copy(status = ReservationStatus.CANCELLED)
        repo.update(id, updated)
        return true
    }

    /**
     * Confirms a reservation. Transitions from PENDING to CONFIRMED.
     */
    suspend fun confirmReservation(id: Uuid): Reservation {
        val reservation = getById(id)
            ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

        if (reservation.status != ReservationStatus.PENDING) {
            throw ApiException(
                HttpStatusCode.BadRequest,
                code = "INVALID_STATUS_TRANSITION",
                message = "Can only confirm pending reservations"
            )
        }

        val updated = reservation.copy(status = ReservationStatus.CONFIRMED)
        return repo.update(id, updated)
            ?: throw ApiException(
                HttpStatusCode.InternalServerError,
                code = "UPDATE_FAILED",
                message = "Failed to update reservation"
            )
    }

    /**
     * Completes a reservation. Transitions from CONFIRMED to COMPLETED.
     * Can be auto-completed if end time has passed.
     */
    suspend fun completeReservation(id: Uuid): Reservation {
        val reservation = getById(id)
            ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

        if (reservation.status != ReservationStatus.CONFIRMED) {
            throw ApiException(
                HttpStatusCode.BadRequest,
                code = "INVALID_STATUS_TRANSITION",
                message = "Can only complete confirmed reservations"
            )
        }

        val updated = reservation.copy(status = ReservationStatus.COMPLETED)
        return repo.update(id, updated)
            ?: throw ApiException(
                HttpStatusCode.InternalServerError,
                code = "UPDATE_FAILED",
                message = "Failed to update reservation"
            )
    }

    /**
     * Adds bonus points to a reservation (typically after driving session).
     */
    suspend fun addPoints(id: Uuid, points: Int): Reservation? {
        val reservation = getById(id) ?: return null
        val updated = reservation.copy(pointsAwarded = reservation.pointsAwarded + points)
        return repo.update(id, updated)
    }

    suspend fun findActiveReservations(now: LocalDateTime): List<Reservation> =
        repo.findAll().filter { it.startTime <= now && it.endTime >= now }

    /**
     * Checks if there are any conflicting reservations for a car in the given time range.
     */
    suspend fun hasConflictingReservations(
        carId: Uuid,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): Boolean {
        val existingReservations = repo.findByCarIdAndTimeRange(carId, startTime, endTime)
        return existingReservations.any { reservation ->
            reservation.status != ReservationStatus.CANCELLED &&
                    reservation.status != ReservationStatus.COMPLETED &&
                    !(endTime <= reservation.startTime || startTime >= reservation.endTime)
        }
    }

    /**
     * Calculates the price for a reservation based on hourly rate and duration.
     */
    private fun calculatePrice(
        ratePerHour: BigDecimal,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): BigDecimal {
        val durationHours = calculateDurationHours(startTime, endTime)
        return ratePerHour * BigDecimal.fromDouble(durationHours)
    }

    /**
     * Calculates duration in hours between two times.
     */
    private fun calculateDurationHours(startTime: LocalDateTime, endTime: LocalDateTime): Double {
        // Convert to instants for proper duration calculation
        val start = startTime.toInstant(TimeZone.UTC)
        val end = endTime.toInstant(TimeZone.UTC)

        val duration = end - start
        return duration.inWholeHours.toDouble() + (duration.inWholeMinutes % 60) / 60.0
    }

    private suspend fun validateForeignKeys(reservation: Reservation) {
        if (!carRepository.existsById(reservation.carId)) {
            throw ApiException(
                http = HttpStatusCode.BadRequest,
                code = "CAR_NOT_FOUND",
                message = "Car with id ${reservation.carId} does not exist"
            )
        }
        if (!userRepository.existsById(reservation.renterId)) {
            throw ApiException(
                http = HttpStatusCode.BadRequest,
                code = "RENTER_NOT_FOUND",
                message = "Renter with id ${reservation.renterId} does not exist"
            )
        }
    }

    /**
     * Get a price quote for a potential reservation without creating it.
     * Useful for showing users the cost before they commit to booking.
     */
    suspend fun getQuote(
        carId: Uuid,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): Triple<BigDecimal, Double, Car> {
        val car = carRepository.findById(carId)
            ?: throw ApiException(HttpStatusCode.NotFound, code = "CAR_NOT_FOUND", message = "Car not found")

        // Validate car is available
        if (!car.isActive) {
            throw ApiException(
                HttpStatusCode.BadRequest,
                code = "CAR_NOT_AVAILABLE",
                message = "Car is not available for rent"
            )
        }

        // Validate time range
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        if (startTime < now) {
            throw ApiException(
                HttpStatusCode.BadRequest,
                code = "PAST_BOOKING_NOT_ALLOWED",
                message = "Cannot create reservations in the past"
            )
        }

        if (endTime <= startTime) {
            throw ApiException(
                HttpStatusCode.BadRequest,
                code = "INVALID_TIME_RANGE",
                message = "End time must be after start time"
            )
        }

        // Validate duration
        val durationHours = calculateDurationHours(startTime, endTime)
        if (durationHours < 1.0) {
            throw ApiException(
                HttpStatusCode.BadRequest,
                code = "DURATION_TOO_SHORT",
                message = "Minimum rental duration is 1 hour"
            )
        }

        if (durationHours > 720.0) {
            throw ApiException(
                HttpStatusCode.BadRequest,
                code = "DURATION_TOO_LONG",
                message = "Maximum rental duration is 30 days"
            )
        }

        // Check for conflicts
        if (hasConflictingReservations(carId, startTime, endTime)) {
            throw ApiException(
                HttpStatusCode.Conflict,
                code = "RESERVATION_CONFLICT",
                message = "This time slot is already reserved"
            )
        }

        // Calculate price
        val price = calculatePrice(car.ratePerHour, startTime, endTime)

        return Triple(price, durationHours, car)
    }

    /**
     * Validates business rules for reservations.
     */
    private suspend fun validateBusinessRules(reservation: Reservation) {
        val car = carRepository.findById(reservation.carId)
            ?: throw ApiException(HttpStatusCode.NotFound, message = "Car not found")

        // Can't rent your own car
        if (car.ownerId == reservation.renterId) {
            throw ApiException(
                HttpStatusCode.BadRequest,
                code = "CANNOT_RENT_OWN_CAR",
                message = "You cannot rent your own car"
            )
        }

        // Car must be active
        if (!car.isActive) {
            throw ApiException(
                HttpStatusCode.BadRequest,
                code = "CAR_NOT_ACTIVE",
                message = "Car is not available for rent"
            )
        }

        // Can't book in the past
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        if (reservation.startTime < now) {
            throw ApiException(
                HttpStatusCode.BadRequest,
                code = "INVALID_TIME_RANGE",
                message = "Cannot create reservations in the past"
            )
        }

        // End must be after start
        if (reservation.endTime <= reservation.startTime) {
            throw ApiException(
                HttpStatusCode.BadRequest,
                code = "INVALID_TIME_RANGE",
                message = "End time must be after start time"
            )
        }

        // Minimum rental duration (1 hour)
        val durationHours = calculateDurationHours(reservation.startTime, reservation.endTime)
        if (durationHours < 1.0) {
            throw ApiException(
                HttpStatusCode.BadRequest,
                code = "INVALID_DURATION",
                message = "Minimum rental duration is 1 hour"
            )
        }

        // Maximum rental duration (30 days)
        if (durationHours > 720.0) {
            throw ApiException(
                HttpStatusCode.BadRequest,
                code = "INVALID_DURATION",
                message = "Maximum rental duration is 30 days"
            )
        }
    }
}
