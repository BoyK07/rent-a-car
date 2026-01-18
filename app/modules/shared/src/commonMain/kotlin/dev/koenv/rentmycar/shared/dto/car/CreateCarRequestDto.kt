package dev.koenv.rentmycar.shared.dto.car

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.koenv.rentmycar.shared.domain.enums.CarCategory
import dev.koenv.rentmycar.shared.domain.enums.FuelType
import dev.koenv.rentmycar.shared.serialization.BigDecimalSerializer
import kotlinx.serialization.Serializable

@Serializable
data class CreateCarRequestDto(
    val brand: String,
    val model: String,
    val category: CarCategory,
    val fuelType: FuelType? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val ratePerHour: BigDecimal,
    val addressLine1: String,
    val addressLine2: String? = null,
    val postalCode: String,
    val city: String,
    val country: String,
    val locationLat: Double? = null,
    val locationLng: Double? = null,
    val isActive: Boolean = true
)
