package dev.koenv.rentmycar.server.mappers.car

import dev.koenv.rentmycar.shared.domain.entity.Car
import dev.koenv.rentmycar.shared.dto.car.CarDto
import dev.koenv.rentmycar.shared.dto.car.CreateCarRequestDto
import dev.koenv.rentmycar.shared.dto.car.PatchCarRequestDto
import dev.koenv.rentmycar.shared.dto.car.UpdateCarRequestDto
import kotlin.uuid.Uuid

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

