package dev.koenv.rentmycar.domain.services

import dev.koenv.rentmycar.domain.model.User
import dev.koenv.rentmycar.domain.repositories.UserRepository

class UserService(private val repo: UserRepository) {
    suspend fun create(user: User): Int = repo.create(user)
    suspend fun read(id: Int): User? = repo.findById(id)
    suspend fun update(id: Int, user: User): Boolean = repo.update(id, user)
    suspend fun delete(id: Int): Boolean = repo.delete(id)
}
