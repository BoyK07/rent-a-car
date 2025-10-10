package dev.koenv.rentmycar.domain.model

import dev.koenv.rentmycar.shared.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class User(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val name: String,
    val age: Int
)

