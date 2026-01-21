package dev.koenv.rentmycar.shared.dto.car

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class UpdateCarAvailabilityRequestDto(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)
