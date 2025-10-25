package dev.koenv.rentmycar.mappers.auth

import dev.koenv.rentmycar.domain.entity.User
import dev.koenv.rentmycar.domain.enums.Role
import dev.koenv.rentmycar.dto.auth.RegisterRequestDto

/** Maps [RegisterRequestDto] to a [User] entity. */
fun RegisterRequestDto.toEntity(passwordHash: String, role: Role): User {
    return User(
        name = name,
        email = email,
        passwordHash = passwordHash,
        role = role
    )
}
