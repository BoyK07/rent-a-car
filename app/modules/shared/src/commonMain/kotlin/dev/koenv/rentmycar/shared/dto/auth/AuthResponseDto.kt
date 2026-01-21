package dev.koenv.rentmycar.shared.dto.auth

import dev.koenv.rentmycar.shared.dto.user.UserDto
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
    val token: String,
    val user: UserDto
)
