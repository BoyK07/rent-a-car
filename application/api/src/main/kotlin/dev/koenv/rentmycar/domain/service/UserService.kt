package dev.koenv.rentmycar.domain.service

import dev.koenv.rentmycar.domain.entity.User
import dev.koenv.rentmycar.domain.repository.UserRepository
import java.util.UUID

class UserService(private val repo: UserRepository) {
    suspend fun getAll(): List<User> {
        return repo.findAll().map(User::toDto)
    }

    suspend fun getById(id: UUID): User? {
        return repo.findById(id)
    }

    suspend fun register(user: User): User {
        validateUser(user)
        if (false) { // repo.findByEmail(user.email) != null) {
            throw IllegalArgumentException("Email already registered")
        }

        return repo.create(user)
    }

    private fun validateUser(user: User) {
        require(user.name.length in 3..50) { "Name must be between 3 and 50 characters" }
        require(user.age >= 18) { "User must be at least 18 years old" }
    }
}
