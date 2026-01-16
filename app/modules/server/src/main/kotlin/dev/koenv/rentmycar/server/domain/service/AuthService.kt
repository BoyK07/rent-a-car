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

/**
 * Service layer for authentication and user registration.
 * 
 * Handles:
 * - User registration with role assignment
 * - Login with JWT token generation
 * - Password hashing and verification
 * - Email uniqueness validation
 * 
 * Security considerations:
 * - Passwords are hashed using Argon2
 * - ADMIN accounts cannot be created via registration
 * - JWT tokens are signed with server secret
 * 
 * @property repo User repository for database operations
 * @property config Application configuration for JWT settings
 */
class AuthService(
    private val repo: UserRepositoryImpl,
    private val config: ApplicationConfig
) {

    /**
     * Registers a new user in the system.
     * 
     * Only DRIVER and MEMBER roles can be registered directly.
     * ADMIN accounts must be created manually for security.
     * 
     * Process:
     * 1. Validates email format and password strength
     * 2. Checks for email uniqueness
     * 3. Validates role is registerable (not ADMIN)
     * 4. Hashes password using Argon2
     * 5. Creates user in database
     * 6. Generates JWT token for immediate authentication
     * 
     * @param req Registration request with email, password, name, and optional role
     * @return AuthResponseDto containing JWT token and user profile
     * @throws IllegalArgumentException if email is already registered, role is invalid, or validation fails
     */
    suspend fun register(req: RegisterRequestDto): AuthResponseDto {
        validateRegistration(req.email, req.password)

        // Check email uniqueness
        if (repo.findByEmail(req.email) != null)
            throw IllegalArgumentException("Email already registered")

        // Validate role is registerable (not ADMIN)
        val selectedRole = req.role ?: Role.MEMBER
        require(Role.isRegisterable(selectedRole)) {
            "Only DRIVER or MEMBER roles can be registered directly"
        }

        // Hash password and create user
        val hash = PasswordUtil.hash(req.password)
        val created = repo.create(req.toEntity(hash, selectedRole))

        val createdUserId = created.id
        require(createdUserId != null) { "User ID must not be null after creation" }

        // Generate JWT token for authentication
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
     * Authenticates a user and issues a JWT token.
     * 
     * Process:
     * 1. Looks up user by email
     * 2. Verifies password hash using Argon2
     * 3. Generates JWT token with user ID and role claims
     * 
     * @param req Login request with email and password
     * @return AuthResponseDto containing JWT token and user profile
     * @throws IllegalArgumentException if credentials are invalid
     */
    suspend fun login(req: LoginRequestDto): AuthResponseDto {
        // Find user by email
        val user = repo.findByEmail(req.email)
            ?: throw IllegalArgumentException("Invalid credentials")

        // Verify password hash
        if (!PasswordUtil.verify(req.password, user.passwordHash))
            throw IllegalArgumentException("Invalid credentials")

        val userId = user.id
        require(userId != null) { "User ID must not be null" }

        // Generate JWT token
        val token = JwtUtil.generateToken(
            userId = userId,
            role = user.role,
            audience = config.property("jwt.audience").getString(),
            issuer = config.property("jwt.domain").getString(),
            secret = config.property("jwt.secret").getString()
        )

        return AuthResponseDto(token, user.toDto())
    }

    /**
     * Validates registration input.
     * 
     * Requirements:
     * - Email must contain @ symbol (basic format check)
     * - Password must be at least 8 characters
     * 
     * @param email Email address to validate
     * @param password Password to validate
     * @throws IllegalArgumentException if validation fails
     */
    private fun validateRegistration(email: String, password: String) {
        require(email.contains("@")) { "Invalid email format" }
        require(password.length >= 8) { "Password must be at least 8 characters" }
    }
}
