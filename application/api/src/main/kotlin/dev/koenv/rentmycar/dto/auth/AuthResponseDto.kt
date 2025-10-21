package dev.koenv.rentmycar.dto.auth

import dev.koenv.rentmycar.dto.user.UserDto
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
    val token: String,
    val user: UserDto
)
