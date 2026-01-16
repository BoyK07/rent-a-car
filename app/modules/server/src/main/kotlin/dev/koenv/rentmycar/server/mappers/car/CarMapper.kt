package dev.koenv.rentmycar.server.mappers.car

import dev.koenv.rentmycar.shared.domain.entity.Car
import dev.koenv.rentmycar.shared.dto.car.CarDto
import dev.koenv.rentmycar.shared.dto.car.CreateCarRequestDto
import dev.koenv.rentmycar.shared.dto.car.PatchCarRequestDto
import dev.koenv.rentmycar.shared.dto.car.UpdateCarRequestDto
import kotlin.uuid.Uuid

/**
 * Converts a Car entity to its DTO representation for API responses.
 * 
 * @receiver Car The car entity to convert
 * @return CarDto The car data transfer object
 * @throws IllegalArgumentException if car ID is null
 */
fun Car.toDto(): CarDto {
    val carId = id
    require(carId != null) { "Cannot convert Car to CarDto: ID is null" }
    return CarDto(
        id = carId,
        ownerId = ownerId,
        brand = brand,
        model = model,
        category = category,
        fuelType = fuelType,
        ratePerHour = ratePerHour,
        locationLat = locationLat,
        locationLng = locationLng,
        isActive = isActive
    )
}

/**
 * Converts a create request to a new Car entity.
 * 
 * @receiver CreateCarRequestDto The creation request from the API
 * @param ownerId The UUID of the user creating/owning this car
 * @return Car The new car entity (ID will be generated on save)
 */
fun CreateCarRequestDto.toEntity(ownerId: Uuid): Car = Car(
    ownerId = ownerId,
    brand = brand,
    model = model,
    category = category,
    fuelType = fuelType,
    ratePerHour = ratePerHour,
    locationLat = locationLat,
    locationLng = locationLng,
    isActive = isActive
)

/**
 * Converts an update request to a Car entity with specified ID.
 * 
 * Used for full replacement updates (PUT operations).
 * 
 * @receiver UpdateCarRequestDto The update request from the API
 * @param id The UUID of the car being updated
 * @param ownerId The UUID of the car owner (immutable)
 * @return Car The updated car entity
 */
fun UpdateCarRequestDto.toEntity(id: Uuid, ownerId: Uuid): Car = Car(
    id = id,
    ownerId = ownerId,
    brand = brand,
    model = model,
    category = category,
    fuelType = fuelType,
    ratePerHour = ratePerHour,
    locationLat = locationLat,
    locationLng = locationLng,
    isActive = isActive
)

/**
 * Applies partial updates to an existing Car entity.
 * 
 * Used for PATCH operations where only specified fields are updated.
 * Null values in the request mean "don't change this field".
 * 
 * @receiver PatchCarRequestDto The patch request with optional fields
 * @param existing The current car entity to patch
 * @return Car The patched car entity with selective updates applied
 * @throws IllegalArgumentException if existing car ID is null
 */
fun PatchCarRequestDto.applyPatch(existing: Car): Car {
    val existingId = existing.id
    require(existingId != null) { "Cannot patch Car: existing Car ID is null" }
    return existing.copy(
        brand = brand ?: existing.brand,
        model = model ?: existing.model,
        category = category ?: existing.category,
        fuelType = fuelType ?: existing.fuelType,
        ratePerHour = ratePerHour ?: existing.ratePerHour,
        locationLat = locationLat ?: existing.locationLat,
        locationLng = locationLng ?: existing.locationLng,
        isActive = isActive ?: existing.isActive
    )
}

