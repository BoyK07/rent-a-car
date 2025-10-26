package dev.koenv.rentmycar.mappers.car

import dev.koenv.rentmycar.domain.entity.CarAvailability
import dev.koenv.rentmycar.dto.car.*
import java.util.UUID

fun CarAvailability.toAvailabilityDto(): CarAvailabilityDto = CarAvailabilityDto(
	id = id ?: throw IllegalStateException("CarAvailability ID is null"),
	carId = carId,
	startTime = startTime,
	endTime = endTime
)

fun CreateCarAvailabilityRequestDto.toAvailabilityEntity(carId: UUID): CarAvailability = CarAvailability(
	carId = carId,
	startTime = startTime,
	endTime = endTime
)

fun UpdateCarAvailabilityRequestDto.toAvailabilityEntity(id: UUID, carId: UUID): CarAvailability = CarAvailability(
	id = id,
	carId = carId,
	startTime = startTime,
	endTime = endTime
)

fun PatchCarAvailabilityRequestDto.applyAvailabilityPatch(existing: CarAvailability): CarAvailability = CarAvailability(
	id = existing.id,
	carId = existing.carId,
	startTime = startTime ?: existing.startTime,
	endTime = endTime ?: existing.endTime
)

