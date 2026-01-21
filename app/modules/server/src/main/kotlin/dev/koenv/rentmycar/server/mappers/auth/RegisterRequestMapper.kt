package dev.koenv.rentmycar.server.mappers.auth

import dev.koenv.rentmycar.shared.domain.entity.User
import dev.koenv.rentmycar.shared.domain.enums.Role
import dev.koenv.rentmycar.shared.dto.auth.RegisterRequestDto

/**
 * Converts a registration request to a User entity.
 * 
 * Combines the request data with the hashed password and assigned role
 * to create a complete user entity for persistence.
 * 
 * @receiver RegisterRequestDto The registration request from the API
 * @param passwordHash The hashed password (never store plain passwords)
 * @param role The role to assign to the new user (MEMBER or DRIVER)
 * @return User The complete user entity ready for database storage
 */
fun RegisterRequestDto.toEntity(passwordHash: String, role: Role): User {
    return User(
        name = name,
        email = email,
        passwordHash = passwordHash,
        role = role
    )
}
