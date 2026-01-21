package dev.koenv.rentmycar.shared.dto.reservation

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * Request DTO for getting a price quote before creating a reservation.
 * Allows users to see the total cost before committing to a booking.
 */
@Serializable
data class ReservationQuoteRequestDto(
    val carId: Uuid,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)
