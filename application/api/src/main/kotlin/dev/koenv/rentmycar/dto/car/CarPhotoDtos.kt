package dev.koenv.rentmycar.dto.car

import dev.koenv.rentmycar.shared.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CarPhotoDto(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	@Serializable(with = UUIDSerializer::class)
	val carId: UUID,
	val url: String,
	val isPrimary: Boolean
)

@Serializable
data class CreateCarPhotoRequestDto(
	val url: String,
	val isPrimary: Boolean = false
)

@Serializable
data class PatchCarPhotoRequestDto(
	val url: String? = null,
	val isPrimary: Boolean? = null
)
