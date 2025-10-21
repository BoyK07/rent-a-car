package dev.koenv.rentmycar.domain.entity

import dev.koenv.rentmycar.shared.serialization.BigDecimalSerializer
import dev.koenv.rentmycar.shared.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.*

@Serializable
data class DrivingSession(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    @Serializable(with = UUIDSerializer::class)
    val reservationId: UUID,
    @Serializable(with = BigDecimalSerializer::class)
    val distanceKm: BigDecimal,
    val harshAccelerations: Int,
    val harshBrakes: Int
)
