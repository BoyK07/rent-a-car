package dev.koenv.rentmycar.dto.auth

import dev.koenv.rentmycar.domain.enums.Role
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String,
    val role: Role? = null
)