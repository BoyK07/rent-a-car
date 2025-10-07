package dev.koenv.rentmycar.domain.repositories

import dev.koenv.rentmycar.domain.model.User

interface UserRepository {
    suspend fun create(user: User): Int
    suspend fun findById(id: Int): User?
    suspend fun update(id: Int, user: User): Boolean
    suspend fun delete(id: Int): Boolean
}
