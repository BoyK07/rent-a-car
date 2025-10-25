package dev.koenv.rentmycar.mappers.car

import dev.koenv.rentmycar.domain.entity.Car
import dev.koenv.rentmycar.dto.car.*
import java.util.UUID

fun Car.toDto(): CarDto = CarDto(
	id = id ?: throw IllegalStateException("Car ID is null"),
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

fun CreateCarRequestDto.toEntity(ownerId: UUID): Car = Car(
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

fun UpdateCarRequestDto.toEntity(id: UUID, ownerId: UUID): Car = Car(
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

fun PatchCarRequestDto.applyPatch(existing: Car): Car = Car(
	id = existing.id,
	ownerId = existing.ownerId,
	brand = brand ?: existing.brand,
	model = model ?: existing.model,
	category = category ?: existing.category,
	fuelType = fuelType ?: existing.fuelType,
	ratePerHour = ratePerHour ?: existing.ratePerHour,
	locationLat = locationLat ?: existing.locationLat,
	locationLng = locationLng ?: existing.locationLng,
	isActive = isActive ?: existing.isActive
)


