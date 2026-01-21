package dev.koenv.rentmycar.shared.dto.car

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CreateCarAvailabilityRequestDto(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)
