package dev.koenv.rentmycar.shared.dto.reservation

import dev.koenv.rentmycar.shared.domain.enums.ReservationStatus
import dev.koenv.rentmycar.shared.serialization.LocalDateTimeEpochSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class UpdateReservationRequestDto(
    val carId: Uuid,
    @Serializable(with = LocalDateTimeEpochSerializer::class)
    val startTime: LocalDateTime,
    @Serializable(with = LocalDateTimeEpochSerializer::class)
    val endTime: LocalDateTime,
    val status: ReservationStatus
    // priceTotal and pointsAwarded are managed server-side
)
