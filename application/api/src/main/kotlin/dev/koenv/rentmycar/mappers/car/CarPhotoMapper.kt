package dev.koenv.rentmycar.mappers.car

import dev.koenv.rentmycar.domain.entity.CarPhoto
import dev.koenv.rentmycar.dto.car.CarPhotoDto
import dev.koenv.rentmycar.dto.car.CreateCarPhotoRequestDto
import dev.koenv.rentmycar.dto.car.PatchCarPhotoRequestDto
import java.util.UUID

fun CarPhoto.toDto(): CarPhotoDto = CarPhotoDto(
	id = id ?: throw IllegalStateException("CarPhoto ID is null"),
	carId = carId,
	url = url,
	isPrimary = isPrimary
)

fun CreateCarPhotoRequestDto.toEntity(carId: UUID): CarPhoto = CarPhoto(
	carId = carId,
	url = url,
	isPrimary = isPrimary
)

fun PatchCarPhotoRequestDto.applyPatch(existing: CarPhoto): CarPhoto = CarPhoto(
	id = existing.id,
	carId = existing.carId,
	url = url ?: existing.url,
	isPrimary = isPrimary ?: existing.isPrimary
)



