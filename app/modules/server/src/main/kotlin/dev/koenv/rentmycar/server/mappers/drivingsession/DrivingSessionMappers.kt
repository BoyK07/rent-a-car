package dev.koenv.rentmycar.server.mappers.drivingsession

import dev.koenv.rentmycar.server.domain.service.DrivingSessionService
import dev.koenv.rentmycar.shared.domain.entity.DrivingSession
import dev.koenv.rentmycar.shared.dto.reservation.CreateDrivingSessionRequestDto
import dev.koenv.rentmycar.shared.dto.reservation.DrivingSessionDto
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.uuid.Uuid

/**
 * Converts a DrivingSession entity to its DTO representation.
 * 
 * @receiver DrivingSession The driving session entity
 * @param pointsEarned Optional calculated points for this session
 * @return DrivingSessionDto The driving session DTO
 * @throws IllegalArgumentException if ID is null
 */
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

/**
 * Converts a create request to a new DrivingSession entity.
 * 
 * Sets creation timestamp to current UTC time.
 * 
 * @receiver CreateDrivingSessionRequestDto The creation request
 * @param reservationId The reservation this session is for
 * @param recordedBy The user recording the session (should be renter)
 * @return DrivingSession The new driving session entity
 */
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

/**
 * Converts a DrivingSession to DTO with calculated points.
 * 
 * Uses the service to calculate points based on driving behavior.
 * 
 * @receiver DrivingSession The driving session entity
 * @param service The service to calculate points with
 * @return DrivingSessionDto The DTO with calculated points
 */
fun DrivingSession.toDto(service: DrivingSessionService) = toDto(
    pointsEarned = service.calculateDrivingPoints(distanceKm, harshAccelerations, harshBrakes)
)
