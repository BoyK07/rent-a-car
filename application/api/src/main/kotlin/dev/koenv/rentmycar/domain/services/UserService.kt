package dev.koenv.rentmycar.domain.services

import dev.koenv.rentmycar.domain.model.User
import dev.koenv.rentmycar.domain.repositories.UserRepository
import java.util.UUID

class UserService(private val repo: UserRepository) {
    suspend fun getAll(): List<User> {
        return repo.findAll()
    }

    suspend fun getById(id: UUID): User? {
        return repo.findById(id)
    }

    suspend fun register(user: User): User {
//        TODO("Implement user registration logic")

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
