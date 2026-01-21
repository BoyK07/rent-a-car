package dev.koenv.rentmycar.shared.dto.reservation

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.koenv.rentmycar.shared.serialization.BigDecimalSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * Response DTO for a reservation price quote.
 * Provides detailed pricing information and car details for user confirmation.
 */
@Serializable
data class ReservationQuoteResponseDto(
    val carId: Uuid,
    val carBrand: String,
    val carModel: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val durationHours: Double,
    @Serializable(with = BigDecimalSerializer::class)
    val ratePerHour: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val totalPrice: BigDecimal
)
