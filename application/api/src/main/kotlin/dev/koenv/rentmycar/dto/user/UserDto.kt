package dev.koenv.rentmycar.dto.user

import dev.koenv.rentmycar.domain.enums.Role
import dev.koenv.rentmycar.shared.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class UserDto(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val name: String,
    val email: String,
    val role: Role
)
