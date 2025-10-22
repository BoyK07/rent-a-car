package dev.koenv.rentmycar.domain.service

import dev.koenv.rentmycar.domain.entity.User
import dev.koenv.rentmycar.domain.repository.UserRepository
import java.util.UUID

class UserService(private val repo: UserRepository) {
    suspend fun getAll(): List<User> = repo.findAll()
    suspend fun getById(id: UUID): User? = repo.findById(id)
    suspend fun create(user: User): User = repo.create(user)
    suspend fun update(id: UUID, user: User): User? = repo.update(id, user)
    suspend fun delete(id: UUID): Boolean = repo.delete(id)
    suspend fun findByEmail(email: String): User? = repo.findByEmail(email)
}
