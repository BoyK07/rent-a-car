package dev.koenv.rentmycar.shared.dto.car

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.koenv.rentmycar.shared.domain.enums.CarCategory
import dev.koenv.rentmycar.shared.domain.enums.FuelType
import dev.koenv.rentmycar.shared.serialization.BigDecimalSerializer
import kotlinx.serialization.Serializable

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
