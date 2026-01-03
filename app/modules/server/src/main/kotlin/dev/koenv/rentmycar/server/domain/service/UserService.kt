package dev.koenv.rentmycar.server.domain.service

import dev.koenv.rentmycar.server.storage.repository.UserRepositoryImpl
import dev.koenv.rentmycar.shared.domain.entity.User
import kotlin.uuid.Uuid

class UserService(private val repo: UserRepositoryImpl) {
    suspend fun getAll(): List<User> = repo.findAll()
    suspend fun getById(id: Uuid): User? = repo.findById(id)
    suspend fun create(user: User): User = repo.create(user)
    suspend fun update(id: Uuid, user: User): User? = repo.update(id, user)
    suspend fun delete(id: Uuid): Boolean = repo.delete(id)
    suspend fun findByEmail(email: String): User? = repo.findByEmail(email)
}
