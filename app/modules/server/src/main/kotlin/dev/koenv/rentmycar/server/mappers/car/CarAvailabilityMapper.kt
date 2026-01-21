package dev.koenv.rentmycar.server.mappers.car

import dev.koenv.rentmycar.shared.domain.entity.CarAvailability
import dev.koenv.rentmycar.shared.dto.car.CarAvailabilityDto
import dev.koenv.rentmycar.shared.dto.car.CreateCarAvailabilityRequestDto
import dev.koenv.rentmycar.shared.dto.car.PatchCarAvailabilityRequestDto
import dev.koenv.rentmycar.shared.dto.car.UpdateCarAvailabilityRequestDto
import kotlin.uuid.Uuid

/**
 * Converts a CarAvailability entity to its DTO representation.
 * 
 * @receiver CarAvailability The availability window entity
 * @return CarAvailabilityDto The availability DTO
 * @throws IllegalArgumentException if ID is null
 */
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

/**
 * Converts a create request to a new CarAvailability entity.
 * 
 * @receiver CreateCarAvailabilityRequestDto The creation request
 * @param carId The car this availability window is for
 * @return CarAvailability The new availability entity
 */
fun CreateCarAvailabilityRequestDto.toAvailabilityEntity(carId: Uuid): CarAvailability = CarAvailability(
    carId = carId,
    startTime = startTime,
    endTime = endTime
)

/**
 * Converts an update request to a CarAvailability entity.
 * 
 * @receiver UpdateCarAvailabilityRequestDto The update request
 * @param id The availability window ID
 * @param carId The car ID (immutable)
 * @return CarAvailability The updated availability entity
 */
fun UpdateCarAvailabilityRequestDto.toAvailabilityEntity(id: Uuid, carId: Uuid): CarAvailability = CarAvailability(
    id = id,
    carId = carId,
    startTime = startTime,
    endTime = endTime
)

/**
 * Applies partial updates to an existing availability window.
 * 
 * @receiver PatchCarAvailabilityRequestDto The patch request
 * @param existing The current availability entity
 * @return CarAvailability The patched entity
 * @throws IllegalArgumentException if existing ID is null
 */
fun PatchCarAvailabilityRequestDto.applyAvailabilityPatch(existing: CarAvailability): CarAvailability {
    val existingId = existing.id
    require(existingId != null) { "Cannot patch CarAvailability: existing CarAvailability ID is null" }
    return existing.copy(
        startTime = startTime ?: existing.startTime,
        endTime = endTime ?: existing.endTime
    )
}

