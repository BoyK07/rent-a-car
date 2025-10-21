package dev.koenv.rentmycar.domain.entity

import dev.koenv.rentmycar.shared.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class CarPhoto(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    @Serializable(with = UUIDSerializer::class)
    val carId: UUID,
    val url: String,
    val isPrimary: Boolean = false
)
