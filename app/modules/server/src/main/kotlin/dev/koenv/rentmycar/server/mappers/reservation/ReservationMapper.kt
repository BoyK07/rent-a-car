package dev.koenv.rentmycar.server.mappers.reservation

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.koenv.rentmycar.shared.domain.entity.Reservation
import dev.koenv.rentmycar.shared.domain.enums.ReservationStatus
import dev.koenv.rentmycar.shared.dto.reservation.CreateReservationRequestDto
import dev.koenv.rentmycar.shared.dto.reservation.PatchReservationRequestDto
import dev.koenv.rentmycar.shared.dto.reservation.ReservationDto
import dev.koenv.rentmycar.shared.dto.reservation.UpdateReservationRequestDto
import kotlin.uuid.Uuid

/**
 * Converts a Reservation entity to its DTO representation.
 * 
 * @receiver Reservation The reservation entity
 * @return ReservationDto The reservation DTO
 * @throws IllegalArgumentException if ID is null
 */
fun Reservation.toDto(): ReservationDto {
    val reservationId = id
    require(reservationId != null) { "Cannot convert Reservation to ReservationDto: ID is null" }
    return ReservationDto(
        id = reservationId,
        carId = carId,
        renterId = renterId,
        startTime = startTime,
        endTime = endTime,
        status = status,
        priceTotal = priceTotal,
        pointsAwarded = pointsAwarded
    )
}

/**
 * Converts a create request to a new Reservation entity.
 * 
 * Initial status is PENDING, price will be calculated server-side.
 * 
 * @receiver CreateReservationRequestDto The creation request
 * @param renterId The user creating the reservation
 * @return Reservation The new reservation entity
 */
fun CreateReservationRequestDto.toEntity(renterId: Uuid): Reservation = Reservation(
    carId = carId,
    renterId = renterId,
    startTime = startTime,
    endTime = endTime,
    status = ReservationStatus.PENDING,
    priceTotal = BigDecimal.ZERO,
    pointsAwarded = 0
)

/**
 * Converts an update request to a Reservation entity.
 * 
 * @receiver UpdateReservationRequestDto The update request
 * @param id The reservation ID
 * @param renterId The renter ID (immutable)
 * @param priceTotal Server-calculated price
 * @param pointsAwarded Server-calculated points
 * @return Reservation The updated reservation entity
 */
fun UpdateReservationRequestDto.toEntity(
    id: Uuid,
    renterId: Uuid,
    priceTotal: BigDecimal,
    pointsAwarded: Int
): Reservation = Reservation(
    id = id,
    carId = carId,
    renterId = renterId,
    startTime = startTime,
    endTime = endTime,
    status = status,
    priceTotal = priceTotal,
    pointsAwarded = pointsAwarded
)

/**
 * Applies partial updates to an existing Reservation.
 * 
 * Note: priceTotal and pointsAwarded are not modifiable via PATCH
 * as they are calculated server-side.
 * 
 * @receiver PatchReservationRequestDto The patch request
 * @param existing The current reservation entity
 * @return Reservation The patched entity
 * @throws IllegalArgumentException if existing ID is null
 */
fun PatchReservationRequestDto.applyPatch(existing: Reservation): Reservation {
    val existingId = existing.id
    require(existingId != null) { "Cannot patch Reservation: existing Reservation ID is null" }
    return existing.copy(
        carId = carId ?: existing.carId,
        startTime = startTime ?: existing.startTime,
        endTime = endTime ?: existing.endTime,
        status = status ?: existing.status
        // priceTotal and pointsAwarded are not modified via PATCH
    )
}

