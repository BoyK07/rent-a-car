package dev.koenv.rentmycar.shared.dto.car

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class CarPhotoDto(
    val id: Uuid,
    val carId: Uuid,
    val url: String,
    val isPrimary: Boolean
)
