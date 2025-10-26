package dev.koenv.rentmycar.dto.car

import dev.koenv.rentmycar.shared.serialization.BigDecimalSerializer
import dev.koenv.rentmycar.shared.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.UUID

@Serializable
data class CarTcoResponseDto(
	@Serializable(with = UUIDSerializer::class)
	val carId: UUID,
	val annualKm: Int,
	@Serializable(with = BigDecimalSerializer::class)
	val tcoPerYear: BigDecimal
)

@Serializable
data class CarCostPerKmResponseDto(
	@Serializable(with = UUIDSerializer::class)
	val carId: UUID,
	@Serializable(with = BigDecimalSerializer::class)
	val costPerKm: BigDecimal
)


