package dev.koenv.rentmycar.shared.dto.user

import dev.koenv.rentmycar.shared.domain.enums.Role
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequestDto(
    val name: String,
    val email: String,
    val role: Role
)
