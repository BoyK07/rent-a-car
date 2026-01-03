package dev.koenv.rentmycar.shared.domain.entity

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class CarAvailability(
    val id: Uuid? = null,
    val carId: Uuid,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)
