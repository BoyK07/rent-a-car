package dev.koenv.rentmycar.domain.entity

import dev.koenv.rentmycar.domain.enums.CarCategory
import dev.koenv.rentmycar.domain.enums.FuelType
import dev.koenv.rentmycar.shared.serialization.BigDecimalSerializer
import dev.koenv.rentmycar.shared.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.*

@Serializable
data class Car(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
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
    val isActive: Boolean = true
)