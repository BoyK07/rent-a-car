package dev.koenv.rentmycar.server.mappers.car

import dev.koenv.rentmycar.shared.domain.entity.CarPhoto
import dev.koenv.rentmycar.shared.dto.car.CarPhotoDto
import dev.koenv.rentmycar.shared.dto.car.CreateCarPhotoRequestDto
import dev.koenv.rentmycar.shared.dto.car.PatchCarPhotoRequestDto
import kotlin.uuid.Uuid

fun CarPhoto.toDto(): CarPhotoDto {
    val photoId = id
    require(photoId != null) { "Cannot convert CarPhoto to CarPhotoDto: ID is null" }
    return CarPhotoDto(
        id = photoId,
        carId = carId,
        url = url,
        isPrimary = isPrimary
    )
}

fun CreateCarPhotoRequestDto.toEntity(carId: Uuid): CarPhoto = CarPhoto(
    carId = carId,
    url = url,
    isPrimary = isPrimary
)

fun PatchCarPhotoRequestDto.applyPatch(existing: CarPhoto): CarPhoto {
    val existingId = existing.id
    require(existingId != null) { "Cannot patch CarPhoto: existing CarPhoto ID is null" }
    return existing.copy(
        url = url ?: existing.url,
        isPrimary = isPrimary ?: existing.isPrimary
    )
}

