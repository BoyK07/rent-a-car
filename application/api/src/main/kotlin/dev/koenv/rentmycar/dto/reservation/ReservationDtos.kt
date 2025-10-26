package dev.koenv.rentmycar.dto.reservation

import dev.koenv.rentmycar.domain.enums.ReservationStatus
import dev.koenv.rentmycar.shared.serialization.BigDecimalSerializer
import dev.koenv.rentmycar.shared.serialization.UUIDSerializer
import dev.koenv.rentmycar.shared.serialization.LocalDateTimeEpochSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.UUID

@Serializable
data class ReservationDto(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	@Serializable(with = UUIDSerializer::class)
	val carId: UUID,
	@Serializable(with = UUIDSerializer::class)
	val renterId: UUID,
	@Serializable(with = LocalDateTimeEpochSerializer::class)
	val startTime: LocalDateTime,
	@Serializable(with = LocalDateTimeEpochSerializer::class)
	val endTime: LocalDateTime,
	val status: ReservationStatus,
	@Serializable(with = BigDecimalSerializer::class)
	val priceTotal: BigDecimal,
	val pointsAwarded: Int
)

@Serializable
data class CreateReservationRequestDto(
	@Serializable(with = UUIDSerializer::class)
	val carId: UUID,
	@Serializable(with = LocalDateTimeEpochSerializer::class)
	val startTime: LocalDateTime,
	@Serializable(with = LocalDateTimeEpochSerializer::class)
	val endTime: LocalDateTime,
	@Serializable(with = BigDecimalSerializer::class)
	val priceTotal: BigDecimal
)

@Serializable
data class UpdateReservationRequestDto(
	@Serializable(with = UUIDSerializer::class)
	val carId: UUID,
	@Serializable(with = LocalDateTimeEpochSerializer::class)
	val startTime: LocalDateTime,
	@Serializable(with = LocalDateTimeEpochSerializer::class)
	val endTime: LocalDateTime,
	val status: ReservationStatus,
	@Serializable(with = BigDecimalSerializer::class)
	val priceTotal: BigDecimal,
	val pointsAwarded: Int
)

@Serializable
data class PatchReservationRequestDto(
	@Serializable(with = UUIDSerializer::class)
	val carId: UUID? = null,
	@Serializable(with = LocalDateTimeEpochSerializer::class)
	val startTime: LocalDateTime? = null,
	@Serializable(with = LocalDateTimeEpochSerializer::class)
	val endTime: LocalDateTime? = null,
	val status: ReservationStatus? = null,
	@Serializable(with = BigDecimalSerializer::class)
	val priceTotal: BigDecimal? = null,
	val pointsAwarded: Int? = null
)


