package dev.koenv.rentmycar.shared.dto.reservation

import dev.koenv.rentmycar.shared.serialization.LocalDateTimeEpochSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class DrivingSessionDto(
    val id: Uuid,
    val reservationId: Uuid,
    @Serializable(with = LocalDateTimeEpochSerializer::class)
    val startTime: LocalDateTime,
    @Serializable(with = LocalDateTimeEpochSerializer::class)
    val endTime: LocalDateTime,
    val distanceKm: Double,
    val harshAccelerations: Int,
    val harshBrakes: Int,
    val recordedBy: Uuid,
    @Serializable(with = LocalDateTimeEpochSerializer::class)
    val createdAt: LocalDateTime,
    val pointsEarned: Int? = null
)
