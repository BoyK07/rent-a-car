package dev.koenv.rentmycar.shared.dto.car

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.koenv.rentmycar.shared.domain.enums.CarCategory
import dev.koenv.rentmycar.shared.domain.enums.FuelType
import dev.koenv.rentmycar.shared.serialization.BigDecimalSerializer
import kotlinx.serialization.Serializable

@Serializable
data class PatchCarRequestDto(
    val brand: String? = null,
    val model: String? = null,
    val category: CarCategory? = null,
    val fuelType: FuelType? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val ratePerHour: BigDecimal? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val postalCode: String? = null,
    val city: String? = null,
    val country: String? = null,
    val locationLat: Double? = null,
    val locationLng: Double? = null,
    val isActive: Boolean? = null
)
