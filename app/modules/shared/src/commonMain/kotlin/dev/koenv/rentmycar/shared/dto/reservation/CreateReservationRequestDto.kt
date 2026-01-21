package dev.koenv.rentmycar.shared.dto.reservation

import dev.koenv.rentmycar.shared.serialization.LocalDateTimeEpochSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class CreateReservationRequestDto(
    val carId: Uuid,
    @Serializable(with = LocalDateTimeEpochSerializer::class)
    val startTime: LocalDateTime,
    @Serializable(with = LocalDateTimeEpochSerializer::class)
    val endTime: LocalDateTime
    // priceTotal is calculated server-side based on car's ratePerHour and duration
)
