package dev.koenv.rentmycar.mappers.user

import dev.koenv.rentmycar.domain.entity.User
import dev.koenv.rentmycar.dto.user.UserDto

fun User.toDto(): UserDto = UserDto(
    id = id ?: throw IllegalStateException("User ID is null"),
    name = name,
    email = email,
    role = role
)
