package dev.koenv.rentmycar.api.v1.auth

import dev.koenv.rentmycar.dto.*
import dev.koenv.rentmycar.domain.entity.Role
import dev.koenv.rentmycar.domain.entity.User
import dev.koenv.rentmycar.shared.util.PasswordUtil
import dev.koenv.rentmycar.shared.util.JwtUtil
import dev.koenv.rentmycar.storage.repository.UserRepositoryImpl
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
    val userRepo = UserRepositoryImpl()
    val config = environment.config

    route("/auth") {
        post("/register") {
            val req = call.receive<RegisterRequest>()
            if (userRepo.findByEmail(req.email) != null) {
                return@post call.respond(HttpStatusCode.Conflict, "Email already registered")
            }
            val user = User(
                name = req.name,
                age = req.age,
                email = req.email,
                passwordHash = PasswordUtil.hash(req.password),
                role = Role.USER
            )
            val created = userRepo.create(user)
            call.respond(HttpStatusCode.Created, created.copy(passwordHash = ""))
        }

        post("/login") {
            val req = call.receive<LoginRequest>()
            val user = userRepo.findByEmail(req.email)
                ?: return@post call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            if (!PasswordUtil.verify(req.password, user.passwordHash)) {
                return@post call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
            val token = JwtUtil.generateToken(
                userId = user.id!!,
                role = user.role,
                audience = config.property("jwt.audience").getString(),
                issuer = config.property("jwt.domain").getString(),
                secret = config.property("jwt.secret").getString()
            )
            call.respond(AuthResponse(token))
        }
    }
}