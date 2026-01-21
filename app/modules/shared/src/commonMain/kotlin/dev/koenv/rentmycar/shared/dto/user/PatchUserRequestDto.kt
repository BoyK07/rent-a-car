package dev.koenv.rentmycar.shared.dto.user

import dev.koenv.rentmycar.shared.domain.enums.Role
import kotlinx.serialization.Serializable

@Serializable
data class PatchUserRequestDto(
    val name: String? = null,
    val email: String? = null,
    val role: Role? = null
)
