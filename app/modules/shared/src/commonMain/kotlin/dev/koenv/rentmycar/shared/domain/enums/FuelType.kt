package dev.koenv.rentmycar.shared.domain.enums

import kotlinx.serialization.Serializable

@Serializable
enum class FuelType {
    PETROL,
    DIESEL,
    LPG,
    ELECTRIC,
    HYBRID,
}
