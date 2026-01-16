package dev.koenv.rentmycar.server.mappers.car

import dev.koenv.rentmycar.shared.domain.entity.CarPhoto
import dev.koenv.rentmycar.shared.dto.car.CarPhotoDto
import dev.koenv.rentmycar.shared.dto.car.CreateCarPhotoRequestDto
import dev.koenv.rentmycar.shared.dto.car.PatchCarPhotoRequestDto
import kotlin.uuid.Uuid

/**
 * Converts a CarPhoto entity to its DTO representation.
 * 
 * @receiver CarPhoto The car photo entity to convert
 * @return CarPhotoDto The car photo data transfer object
 * @throws IllegalArgumentException if photo ID is null
 */
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

/**
 * Converts a create request to a new CarPhoto entity.
 * 
 * @receiver CreateCarPhotoRequestDto The creation request from the API
 * @param carId The UUID of the car this photo belongs to
 * @return CarPhoto The new photo entity (ID will be generated on save)
 */
fun CreateCarPhotoRequestDto.toEntity(carId: Uuid): CarPhoto = CarPhoto(
    carId = carId,
    url = url,
    isPrimary = isPrimary
)

/**
 * Applies partial updates to an existing CarPhoto entity.
 * 
 * Used for PATCH operations. Null values mean "don't change this field".
 * 
 * @receiver PatchCarPhotoRequestDto The patch request with optional fields
 * @param existing The current photo entity to patch
 * @return CarPhoto The patched photo entity
 * @throws IllegalArgumentException if existing photo ID is null
 */
fun PatchCarPhotoRequestDto.applyPatch(existing: CarPhoto): CarPhoto {
    val existingId = existing.id
    require(existingId != null) { "Cannot patch CarPhoto: existing CarPhoto ID is null" }
    return existing.copy(
        url = url ?: existing.url,
        isPrimary = isPrimary ?: existing.isPrimary
    )
}

