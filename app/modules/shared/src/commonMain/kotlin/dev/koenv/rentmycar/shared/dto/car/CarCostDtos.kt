package dev.koenv.rentmycar.shared.dto.car

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.koenv.rentmycar.shared.serialization.BigDecimalSerializer
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class CarTcoResponseDto(
    val carId: Uuid,
    val annualKm: Int,
    @Serializable(with = BigDecimalSerializer::class)
    val tcoPerYear: BigDecimal
)

@Serializable
data class CarCostPerKmResponseDto(
    val carId: Uuid,
    @Serializable(with = BigDecimalSerializer::class)
    val costPerKm: BigDecimal
)
