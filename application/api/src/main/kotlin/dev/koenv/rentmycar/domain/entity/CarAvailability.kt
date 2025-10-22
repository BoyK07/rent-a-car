package dev.koenv.rentmycar.domain.entity

import dev.koenv.rentmycar.shared.serialization.UUIDSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class CarAvailability(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    @Serializable(with = UUIDSerializer::class)
    val carId: UUID,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)
