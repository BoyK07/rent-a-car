package dev.koenv.rentmycar.server.mappers.user

import dev.koenv.rentmycar.shared.domain.entity.User
import dev.koenv.rentmycar.shared.dto.user.UserDto

fun User.toDto(): UserDto {
    val userId = id
    require(userId != null) { "Cannot convert User to UserDto: ID is null" }
    return UserDto(
        id = userId,
        name = name,
        email = email,
        role = role
    )
}

