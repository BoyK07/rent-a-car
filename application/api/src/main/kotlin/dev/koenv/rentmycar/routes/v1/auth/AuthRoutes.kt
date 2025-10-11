package dev.koenv.rentmycar.routes.v1.auth

import dev.koenv.rentmycar.domain.service.AuthService
import dev.koenv.rentmycar.dto.auth.LoginRequestDto
import dev.koenv.rentmycar.dto.auth.RegisterRequestDto
import dev.koenv.rentmycar.routes.RouteRegistrar
import dev.koenv.rentmycar.storage.repository.UserRepositoryImpl
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*

object AuthRoutes : RouteRegistrar {
    override fun Route.register() {
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
}
