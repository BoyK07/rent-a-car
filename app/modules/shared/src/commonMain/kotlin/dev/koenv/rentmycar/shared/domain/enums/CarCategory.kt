package dev.koenv.rentmycar.shared.domain.enums

import kotlinx.serialization.Serializable

@Serializable
enum class CarCategory(val label: String) {
    ICE("Internal Combustion Engine"),
    BEV("Battery Electric Vehicle"),
    FCEV("Fuel Cell Electric Vehicle");
}
