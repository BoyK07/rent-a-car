package dev.koenv.rentmycar.routes.api.v1.auth

import dev.koenv.rentmycar.domain.service.AuthService
import dev.koenv.rentmycar.dto.auth.LoginRequestDto
import dev.koenv.rentmycar.dto.auth.RegisterRequestDto
import dev.koenv.rentmycar.routes.RouteRegistrar
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

object AuthRoutes : RouteRegistrar {
    override fun Route.register() {
        val authService by inject<AuthService>()

        route("/auth") {
            post("/register") {
                val req = call.receive<RegisterRequestDto>()
                runCatching { authService.register(req) }
                    .onSuccess { call.respond(HttpStatusCode.Created, it) }
                    .onFailure { call.respond(HttpStatusCode.Conflict, mapOf("error" to it.message)) }
            }

            post("/login") {
                val req = call.receive<LoginRequestDto>()
                runCatching { authService.login(req) }
                    .onSuccess { call.respond(HttpStatusCode.OK, it) }
                    .onFailure { call.respond(HttpStatusCode.Unauthorized, mapOf("error" to it.message)) }
            }
        }
    }
}
