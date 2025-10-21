package dev.koenv.rentmycar.domain.repository

import dev.koenv.rentmycar.domain.entity.User
import java.util.UUID

interface UserRepository : Repository<User, UUID> {
    suspend fun findByEmail(email: String): User?
}
