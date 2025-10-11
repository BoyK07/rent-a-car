package dev.koenv.rentmycar.api.v1.auth

import dev.koenv.rentmycar.domain.service.AuthService
import dev.koenv.rentmycar.dto.auth.*
import dev.koenv.rentmycar.storage.repository.UserRepositoryImpl
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
    val service = AuthService(UserRepositoryImpl(), environment.config)

    route("/auth") {
        post("/register") {
            val req = call.receive<RegisterRequestDto>()
            runCatching { service.register(req) }
                .onSuccess { call.respond(HttpStatusCode.Created, it) }
                .onFailure { call.respond(HttpStatusCode.Conflict, mapOf("error" to it.message)) }
        }

        post("/login") {
            val req = call.receive<LoginRequestDto>()
            runCatching { service.login(req) }
                .onSuccess { call.respond(HttpStatusCode.OK, it) }
                .onFailure { call.respond(HttpStatusCode.Unauthorized, mapOf("error" to it.message)) }
        }
    }
}
