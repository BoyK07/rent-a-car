package dev.koenv.rentmycar.shared.domain.entity

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.koenv.rentmycar.shared.domain.enums.CarCategory
import dev.koenv.rentmycar.shared.domain.enums.FuelType
import dev.koenv.rentmycar.shared.serialization.BigDecimalSerializer
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Car(
    val id: Uuid? = null,
    val ownerId: Uuid,
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
