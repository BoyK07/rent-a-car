package dev.koenv.rentmycar.server.mappers.user

import dev.koenv.rentmycar.shared.domain.entity.User
import dev.koenv.rentmycar.shared.dto.user.UserDto

/**
 * Converts a User entity to its DTO representation.
 * 
 * Excludes sensitive fields like passwordHash for API responses.
 * 
 * @receiver User The user entity to convert
 * @return UserDto The user data transfer object
 * @throws IllegalArgumentException if user ID is null
 */
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

