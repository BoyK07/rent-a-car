package dev.koenv.rentmycar.shared.dto.car

import kotlinx.serialization.Serializable

@Serializable
data class PatchCarPhotoRequestDto(
    val url: String? = null,
    val isPrimary: Boolean? = null
)
