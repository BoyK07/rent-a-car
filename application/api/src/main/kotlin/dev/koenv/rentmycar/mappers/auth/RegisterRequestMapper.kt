package dev.koenv.rentmycar.mappers.auth

import dev.koenv.rentmycar.domain.enums.Role
import dev.koenv.rentmycar.domain.entity.User
import dev.koenv.rentmycar.dto.auth.RegisterRequestDto

fun RegisterRequestDto.toEntity(passwordHash: String): User = User(
    name = name,
    email = email,
    passwordHash = passwordHash,
    role = Role.DRIVER
)
