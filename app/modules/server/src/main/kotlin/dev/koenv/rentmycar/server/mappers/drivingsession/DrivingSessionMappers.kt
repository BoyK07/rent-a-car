package dev.koenv.rentmycar.server.mappers.drivingsession

import dev.koenv.rentmycar.server.domain.service.DrivingSessionService
import dev.koenv.rentmycar.shared.domain.entity.DrivingSession
import dev.koenv.rentmycar.shared.dto.reservation.CreateDrivingSessionRequestDto
import dev.koenv.rentmycar.shared.dto.reservation.DrivingSessionDto
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.uuid.Uuid

fun DrivingSession.toDto(pointsEarned: Int? = null): DrivingSessionDto {
    val sessionId = id
    require(sessionId != null) { "Cannot convert DrivingSession to DrivingSessionDto: ID is null" }
    return DrivingSessionDto(
        id = sessionId,
        reservationId = reservationId,
        startTime = startTime,
        endTime = endTime,
        distanceKm = distanceKm,
        harshAccelerations = harshAccelerations,
        harshBrakes = harshBrakes,
        recordedBy = recordedBy,
        createdAt = createdAt,
        pointsEarned = pointsEarned
    )
}

fun CreateDrivingSessionRequestDto.toEntity(reservationId: Uuid, recordedBy: Uuid) = DrivingSession(
    id = null,
    reservationId = reservationId,
    startTime = startTime,
    endTime = endTime,
    distanceKm = distanceKm,
    harshAccelerations = harshAccelerations,
    harshBrakes = harshBrakes,
    recordedBy = recordedBy,
    createdAt = Clock.System.now().toLocalDateTime(TimeZone.UTC)
)

fun DrivingSession.toDto(service: DrivingSessionService) = toDto(
    pointsEarned = service.calculateDrivingPoints(distanceKm, harshAccelerations, harshBrakes)
)
