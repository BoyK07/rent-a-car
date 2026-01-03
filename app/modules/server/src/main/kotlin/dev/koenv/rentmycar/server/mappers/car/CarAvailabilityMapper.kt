package dev.koenv.rentmycar.server.mappers.car

import dev.koenv.rentmycar.shared.domain.entity.CarAvailability
import dev.koenv.rentmycar.shared.dto.car.CarAvailabilityDto
import dev.koenv.rentmycar.shared.dto.car.CreateCarAvailabilityRequestDto
import dev.koenv.rentmycar.shared.dto.car.PatchCarAvailabilityRequestDto
import dev.koenv.rentmycar.shared.dto.car.UpdateCarAvailabilityRequestDto
import kotlin.uuid.Uuid

fun CarAvailability.toAvailabilityDto(): CarAvailabilityDto {
    val availabilityId = id
    require(availabilityId != null) { "Cannot convert CarAvailability to CarAvailabilityDto: ID is null" }
    return CarAvailabilityDto(
        id = availabilityId,
        carId = carId,
        startTime = startTime,
        endTime = endTime
    )
}

fun CreateCarAvailabilityRequestDto.toAvailabilityEntity(carId: Uuid): CarAvailability = CarAvailability(
    carId = carId,
    startTime = startTime,
    endTime = endTime
)

fun UpdateCarAvailabilityRequestDto.toAvailabilityEntity(id: Uuid, carId: Uuid): CarAvailability = CarAvailability(
    id = id,
    carId = carId,
    startTime = startTime,
    endTime = endTime
)

fun PatchCarAvailabilityRequestDto.applyAvailabilityPatch(existing: CarAvailability): CarAvailability {
    val existingId = existing.id
    require(existingId != null) { "Cannot patch CarAvailability: existing CarAvailability ID is null" }
    return existing.copy(
        startTime = startTime ?: existing.startTime,
        endTime = endTime ?: existing.endTime
    )
}

