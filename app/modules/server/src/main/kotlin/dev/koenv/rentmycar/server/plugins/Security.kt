package dev.koenv.rentmycar.server.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.koenv.rentmycar.server.util.respondError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.callid.*

fun Application.configureSecurity() {
    val config = environment.config
    val jwtAudience = config.property("jwt.audience").getString()
    val jwtDomain = config.property("jwt.domain").getString()
    val jwtRealm = config.property("jwt.realm").getString()
    val jwtSecret = config.property("jwt.secret").getString()

    authentication {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .build()
            )
            validate { cred ->
                val userId = cred.payload.getClaim("userId").asString()
                val role = cred.payload.getClaim("role").asString()
                if (userId != null && role != null) JWTPrincipal(cred.payload) else null
            }
            // Return unified ApiResponse format for 401 errors
            challenge { _, _ ->
                call.respondError(
                    status = HttpStatusCode.Unauthorized,
                    message = "Invalid or missing authentication token",
                    code = "UNAUTHORIZED",
                    traceId = call.callId
                )
            }
        }
    }
}
