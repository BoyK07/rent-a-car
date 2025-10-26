package dev.koenv.rentmycar.mappers.reservation

import dev.koenv.rentmycar.domain.entity.Reservation
import dev.koenv.rentmycar.domain.enums.ReservationStatus
import dev.koenv.rentmycar.dto.reservation.CreateReservationRequestDto
import dev.koenv.rentmycar.dto.reservation.PatchReservationRequestDto
import dev.koenv.rentmycar.dto.reservation.ReservationDto
import dev.koenv.rentmycar.dto.reservation.UpdateReservationRequestDto
import java.util.UUID

fun Reservation.toDto(): ReservationDto = ReservationDto(
	id = id ?: throw IllegalStateException("Reservation ID is null"),
	carId = carId,
	renterId = renterId,
	startTime = startTime,
	endTime = endTime,
	status = status,
	priceTotal = priceTotal,
	pointsAwarded = pointsAwarded
)

fun CreateReservationRequestDto.toEntity(renterId: UUID): Reservation = Reservation(
	carId = carId,
	renterId = renterId,
	startTime = startTime,
	endTime = endTime,
	status = ReservationStatus.PENDING,
	priceTotal = priceTotal,
	pointsAwarded = 0
)

fun UpdateReservationRequestDto.toEntity(id: UUID, renterId: UUID): Reservation = Reservation(
	id = id,
	carId = carId,
	renterId = renterId,
	startTime = startTime,
	endTime = endTime,
	status = status,
	priceTotal = priceTotal,
	pointsAwarded = pointsAwarded
)

fun PatchReservationRequestDto.applyPatch(existing: Reservation): Reservation = Reservation(
	id = existing.id,
	carId = carId ?: existing.carId,
	renterId = existing.renterId,
	startTime = startTime ?: existing.startTime,
	endTime = endTime ?: existing.endTime,
	status = status ?: existing.status,
	priceTotal = priceTotal ?: existing.priceTotal,
	pointsAwarded = pointsAwarded ?: existing.pointsAwarded
)


