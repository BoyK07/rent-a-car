package dev.koenv.rentmycar.domain.service

import dev.koenv.rentmycar.domain.entity.User
import dev.koenv.rentmycar.domain.repository.UserRepository
import java.util.*

class UserService(private val repo: UserRepository) {

    suspend fun getAll(): List<User> = repo.findAll()

    suspend fun getById(id: UUID): User? = repo.findById(id)

    suspend fun delete(id: UUID): Boolean = repo.delete(id)
}
