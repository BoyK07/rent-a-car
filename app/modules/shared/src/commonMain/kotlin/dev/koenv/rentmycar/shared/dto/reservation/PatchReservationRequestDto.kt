package dev.koenv.rentmycar.shared.dto.reservation

import dev.koenv.rentmycar.shared.domain.enums.ReservationStatus
import dev.koenv.rentmycar.shared.serialization.LocalDateTimeEpochSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class PatchReservationRequestDto(
    val carId: Uuid? = null,
    @Serializable(with = LocalDateTimeEpochSerializer::class)
    val startTime: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeEpochSerializer::class)
    val endTime: LocalDateTime? = null,
    val status: ReservationStatus? = null
    // priceTotal and pointsAwarded cannot be modified via PATCH - managed server-side
)
