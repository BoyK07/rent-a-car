package dev.koenv.rentmycar.shared.dto.car

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class PatchCarAvailabilityRequestDto(
    val startTime: LocalDateTime? = null,
    val endTime: LocalDateTime? = null
)
