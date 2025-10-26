package dev.koenv.rentmycar.dto.car

import dev.koenv.rentmycar.shared.serialization.UUIDSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CarAvailabilityDto(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	@Serializable(with = UUIDSerializer::class)
	val carId: UUID,
	val startTime: LocalDateTime,
	val endTime: LocalDateTime
)

@Serializable
data class CreateCarAvailabilityRequestDto(
	val startTime: LocalDateTime,
	val endTime: LocalDateTime
)

@Serializable
data class UpdateCarAvailabilityRequestDto(
	val startTime: LocalDateTime,
	val endTime: LocalDateTime
)

@Serializable
data class PatchCarAvailabilityRequestDto(
	val startTime: LocalDateTime? = null,
	val endTime: LocalDateTime? = null
)

