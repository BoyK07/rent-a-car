package dev.koenv.rentmycar.shared.domain.enums

import kotlinx.serialization.Serializable

@Serializable
enum class ReservationStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
}
