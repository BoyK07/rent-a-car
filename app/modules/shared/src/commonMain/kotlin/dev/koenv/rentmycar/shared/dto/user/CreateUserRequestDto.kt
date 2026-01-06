package dev.koenv.rentmycar.shared.dto.user

import dev.koenv.rentmycar.shared.domain.enums.Role
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequestDto(
    val name: String,
    val email: String,
    val password: String,
    val role: Role
)
