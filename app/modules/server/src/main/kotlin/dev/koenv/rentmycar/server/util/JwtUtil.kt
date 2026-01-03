package dev.koenv.rentmycar.server.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.koenv.rentmycar.shared.domain.enums.Role
import java.util.*
import kotlin.uuid.Uuid

object JwtUtil {
    fun generateToken(
        userId: Uuid,
        role: Role,
        audience: String,
        issuer: String,
        secret: String,
        // expiresInSeconds: Long = 3600 // 1 hour
        expiresInSeconds: Long = 2592000 // 1 month
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
