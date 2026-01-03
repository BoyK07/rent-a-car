package dev.koenv.rentmycar.server.mappers.auth

import dev.koenv.rentmycar.shared.domain.entity.User
import dev.koenv.rentmycar.shared.domain.enums.Role
import dev.koenv.rentmycar.shared.dto.auth.RegisterRequestDto

/** Maps [RegisterRequestDto] to a [User] entity. */
fun RegisterRequestDto.toEntity(passwordHash: String, role: Role): User {
    return User(
        name = name,
        email = email,
        passwordHash = passwordHash,
        role = role
    )
}
