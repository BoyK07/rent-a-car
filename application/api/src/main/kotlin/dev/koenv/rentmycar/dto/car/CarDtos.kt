package dev.koenv.rentmycar.dto.car

import dev.koenv.rentmycar.domain.enums.CarCategory
import dev.koenv.rentmycar.domain.enums.FuelType
import dev.koenv.rentmycar.shared.serialization.BigDecimalSerializer
import dev.koenv.rentmycar.shared.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.UUID

@Serializable
data class CarDto(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	@Serializable(with = UUIDSerializer::class)
	val ownerId: UUID,
	val brand: String,
	val model: String,
	val category: CarCategory,
	val fuelType: FuelType? = null,
	@Serializable(with = BigDecimalSerializer::class)
	val ratePerHour: BigDecimal,
	val locationLat: Double,
	val locationLng: Double,
	val isActive: Boolean
)

@Serializable
data class CreateCarRequestDto(
	val brand: String,
	val model: String,
	val category: CarCategory,
	val fuelType: FuelType? = null,
	@Serializable(with = BigDecimalSerializer::class)
	val ratePerHour: BigDecimal,
	val locationLat: Double,
	val locationLng: Double,
	val isActive: Boolean = true
)

@Serializable
data class UpdateCarRequestDto(
	val brand: String,
	val model: String,
	val category: CarCategory,
	val fuelType: FuelType? = null,
	@Serializable(with = BigDecimalSerializer::class)
	val ratePerHour: BigDecimal,
	val locationLat: Double,
	val locationLng: Double,
	val isActive: Boolean
)

@Serializable
data class PatchCarRequestDto(
	val brand: String? = null,
	val model: String? = null,
	val category: CarCategory? = null,
	val fuelType: FuelType? = null,
	@Serializable(with = BigDecimalSerializer::class)
	val ratePerHour: BigDecimal? = null,
	val locationLat: Double? = null,
	val locationLng: Double? = null,
	val isActive: Boolean? = null
)


