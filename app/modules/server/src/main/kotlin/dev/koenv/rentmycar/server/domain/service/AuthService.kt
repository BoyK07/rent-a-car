package dev.koenv.rentmycar.server.domain.service

import dev.koenv.rentmycar.server.mappers.auth.toEntity
import dev.koenv.rentmycar.server.mappers.user.toDto
import dev.koenv.rentmycar.server.storage.repository.UserRepositoryImpl
import dev.koenv.rentmycar.server.util.JwtUtil
import dev.koenv.rentmycar.server.util.PasswordUtil
import dev.koenv.rentmycar.shared.domain.enums.Role
import dev.koenv.rentmycar.shared.dto.auth.AuthResponseDto
import dev.koenv.rentmycar.shared.dto.auth.LoginRequestDto
import dev.koenv.rentmycar.shared.dto.auth.RegisterRequestDto
import io.ktor.server.config.*

class AuthService(
    private val repo: UserRepositoryImpl,
    private val config: ApplicationConfig
) {

    /**
     * Registers a new user as DRIVER or MEMBER.
     * ADMIN accounts must be created/updated manually.
     */
    suspend fun register(req: RegisterRequestDto): AuthResponseDto {
        validateRegistration(req.email, req.password)

        if (repo.findByEmail(req.email) != null)
            throw IllegalArgumentException("Email already registered")

        val selectedRole = req.role ?: Role.MEMBER
        require(Role.isRegisterable(selectedRole)) {
            "Only DRIVER or MEMBER roles can be registered directly"
        }

        val hash = PasswordUtil.hash(req.password)
        val created = repo.create(req.toEntity(hash, selectedRole))

        val createdUserId = created.id
        require(createdUserId != null) { "User ID must not be null after creation" }

        val token = JwtUtil.generateToken(
            userId = createdUserId,
            role = created.role,
            audience = config.property("jwt.audience").getString(),
            issuer = config.property("jwt.domain").getString(),
            secret = config.property("jwt.secret").getString()
        )

        return AuthResponseDto(token, created.toDto())
    }

    /**
     * Logs in a user and issues a JWT if credentials are valid.
     */
    suspend fun login(req: LoginRequestDto): AuthResponseDto {
        val user = repo.findByEmail(req.email)
            ?: throw IllegalArgumentException("Invalid credentials")

        if (!PasswordUtil.verify(req.password, user.passwordHash))
            throw IllegalArgumentException("Invalid credentials")

        val userId = user.id
        require(userId != null) { "User ID must not be null" }

        val token = JwtUtil.generateToken(
            userId = userId,
            role = user.role,
            audience = config.property("jwt.audience").getString(),
            issuer = config.property("jwt.domain").getString(),
            secret = config.property("jwt.secret").getString()
        )

        return AuthResponseDto(token, user.toDto())
    }

    private fun validateRegistration(email: String, password: String) {
        require(email.contains("@")) { "Invalid email format" }
        require(password.length >= 8) { "Password must be at least 8 characters" }
    }
}
