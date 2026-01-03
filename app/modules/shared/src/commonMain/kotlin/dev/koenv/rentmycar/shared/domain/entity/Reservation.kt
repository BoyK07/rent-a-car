package dev.koenv.rentmycar.shared.domain.entity

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.koenv.rentmycar.shared.domain.enums.ReservationStatus
import dev.koenv.rentmycar.shared.serialization.BigDecimalSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Reservation(
    val id: Uuid? = null,
    val carId: Uuid,
    val renterId: Uuid,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val status: ReservationStatus,
    @Serializable(with = BigDecimalSerializer::class)
    val priceTotal: BigDecimal,
    val pointsAwarded: Int = 0
)
