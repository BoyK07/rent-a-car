package dev.koenv.rentmycar.mappers.auth

import dev.koenv.rentmycar.domain.entity.Role
import dev.koenv.rentmycar.domain.entity.User
import dev.koenv.rentmycar.dto.auth.RegisterRequestDto

fun RegisterRequestDto.toEntity(passwordHash: String): User = User(
    email = email,
    passwordHash = passwordHash,
    role = Role.DRIVER
)
