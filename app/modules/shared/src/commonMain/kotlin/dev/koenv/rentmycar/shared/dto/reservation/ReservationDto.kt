package dev.koenv.rentmycar.shared.dto.reservation

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.koenv.rentmycar.shared.domain.enums.ReservationStatus
import dev.koenv.rentmycar.shared.serialization.BigDecimalSerializer
import dev.koenv.rentmycar.shared.serialization.LocalDateTimeEpochSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class ReservationDto(
    val id: Uuid,
    val carId: Uuid,
    val renterId: Uuid,
    @Serializable(with = LocalDateTimeEpochSerializer::class)
    val startTime: LocalDateTime,
    @Serializable(with = LocalDateTimeEpochSerializer::class)
    val endTime: LocalDateTime,
    val status: ReservationStatus,
    @Serializable(with = BigDecimalSerializer::class)
    val priceTotal: BigDecimal,
    val pointsAwarded: Int
)
