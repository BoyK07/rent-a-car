package dev.koenv.rentmycar.shared.dto.user

import dev.koenv.rentmycar.shared.domain.enums.Role
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class UserDto(
    val id: Uuid,
    val name: String,
    val email: String,
    val role: Role
)
