package dev.koenv.rentmycar.shared.domain.entity

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class DrivingSession(
    val id: Uuid? = null,
    val reservationId: Uuid,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val distanceKm: Double,
    val harshAccelerations: Int,
    val harshBrakes: Int,
    val recordedBy: Uuid, // User who recorded this session
    val createdAt: LocalDateTime
)
