package dev.koenv.rentmycar.shared.domain.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class CarPhoto(
    val id: Uuid? = null,
    val carId: Uuid,
    val url: String,
    val isPrimary: Boolean = false
)
