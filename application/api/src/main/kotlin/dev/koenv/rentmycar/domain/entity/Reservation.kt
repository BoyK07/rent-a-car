package dev.koenv.rentmycar.domain.entity

import dev.koenv.rentmycar.domain.enums.ReservationStatus
import dev.koenv.rentmycar.shared.serialization.BigDecimalSerializer
import dev.koenv.rentmycar.shared.serialization.UUIDSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.*

@Serializable
data class Reservation(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    @Serializable(with = UUIDSerializer::class)
    val carId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val renterId: UUID,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val status: ReservationStatus,
    @Serializable(with = BigDecimalSerializer::class)
    val priceTotal: BigDecimal,
    val pointsAwarded: Int = 0
)
