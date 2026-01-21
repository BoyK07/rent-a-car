package dev.koenv.rentmycar.shared.dto.car

import kotlinx.serialization.Serializable

@Serializable
data class CreateCarPhotoRequestDto(
    val url: String,
    val isPrimary: Boolean = false
)
