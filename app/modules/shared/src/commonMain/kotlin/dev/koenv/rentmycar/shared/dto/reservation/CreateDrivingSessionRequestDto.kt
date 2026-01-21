package dev.koenv.rentmycar.shared.dto.reservation

import dev.koenv.rentmycar.shared.serialization.LocalDateTimeEpochSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CreateDrivingSessionRequestDto(
    @Serializable(with = LocalDateTimeEpochSerializer::class)
    val startTime: LocalDateTime,
    @Serializable(with = LocalDateTimeEpochSerializer::class)
    val endTime: LocalDateTime,
    val distanceKm: Double,
    val harshAccelerations: Int,
    val harshBrakes: Int
)
