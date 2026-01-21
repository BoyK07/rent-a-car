package dev.koenv.rentmycar.server.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.koenv.rentmycar.shared.domain.enums.Role
import java.util.*
import kotlin.uuid.Uuid

/**
 * Utility for generating JWT tokens for authentication.
 * 
 * Tokens contain:
 * - userId: The user's UUID
 * - role: The user's role (MEMBER, DRIVER, or ADMIN)
 * - Standard JWT claims (audience, issuer, expiration)
 * 
 * Tokens are signed using HMAC256 with the server secret.
 */
object JwtUtil {
    /**
     * Generates a JWT token for a user.
     * 
     * Token expiration is set to 1 month by default to maintain user sessions.
     * 
     * @param userId The user's unique identifier
     * @param role The user's role for authorization
     * @param audience The intended audience for the token
     * @param issuer The token issuer (typically the server domain)
     * @param secret The secret key for signing the token
     * @param expiresInSeconds Token lifetime in seconds (default: 1 month)
     * @return Signed JWT token string
     */
    fun generateToken(
        userId: Uuid,
        role: Role,
        audience: String,
        issuer: String,
        secret: String,
        expiresInSeconds: Long = 2592000 // 1 month (30 days)
    ): String {
        val now = System.currentTimeMillis()
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId.toString())
            .withClaim("role", role.name)
            .withExpiresAt(Date(now + expiresInSeconds * 1000))
            .sign(Algorithm.HMAC256(secret))
    }
}
