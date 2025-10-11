package dev.koenv.rentmycar.mappers.user

import dev.koenv.rentmycar.domain.entity.User
import dev.koenv.rentmycar.dto.user.UserDto
import java.util.*

fun User.toDto(): UserDto = UserDto(
    id = id ?: throw IllegalStateException("User ID is null"),
    email = email,
    role = role
)
