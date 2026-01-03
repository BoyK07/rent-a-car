package dev.koenv.rentmycar.shared.dto.car

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class CarAvailabilityDto(
    val id: Uuid,
    val carId: Uuid,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)
