package dev.koenv.rentmycar.server.mappers.reservation

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.koenv.rentmycar.shared.domain.entity.Reservation
import dev.koenv.rentmycar.shared.domain.enums.ReservationStatus
import dev.koenv.rentmycar.shared.dto.reservation.CreateReservationRequestDto
import dev.koenv.rentmycar.shared.dto.reservation.PatchReservationRequestDto
import dev.koenv.rentmycar.shared.dto.reservation.ReservationDto
import dev.koenv.rentmycar.shared.dto.reservation.UpdateReservationRequestDto
import kotlin.uuid.Uuid

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

fun CreateReservationRequestDto.toEntity(renterId: Uuid): Reservation = Reservation(
    carId = carId,
    renterId = renterId,
    startTime = startTime,
    endTime = endTime,
    status = ReservationStatus.PENDING,
    priceTotal = BigDecimal.ZERO,
    pointsAwarded = 0
)

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

