package dev.koenv.rentmycar.server.routes.api.v1.auth

import dev.koenv.rentmycar.server.domain.service.AuthService
import dev.koenv.rentmycar.server.routes.RouteRegistrar
import dev.koenv.rentmycar.server.util.requireBodyOrFail
import dev.koenv.rentmycar.shared.dto.auth.LoginRequestDto
import dev.koenv.rentmycar.shared.dto.auth.RegisterRequestDto
import dev.koenv.rentmycar.shared.http.ApiException
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

object AuthRoutes : RouteRegistrar {
    override fun Route.register() {
        val authService by inject<AuthService>()

        route("/auth") {
            post("/register") {
                val req = call.requireBodyOrFail<RegisterRequestDto>()
                try {
                    val response = authService.register(req)
                    call.respond(HttpStatusCode.Created, response)
                } catch (e: IllegalStateException) {
                    throw ApiException(HttpStatusCode.Conflict, message = e.message ?: "Registration failed")
                } catch (e: IllegalArgumentException) {
                    throw ApiException(HttpStatusCode.BadRequest, message = e.message ?: "Invalid registration data")
                }
            }

            post("/login") {
                val req = call.requireBodyOrFail<LoginRequestDto>()
                try {
                    val response = authService.login(req)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: IllegalStateException) {
                    throw ApiException(HttpStatusCode.Unauthorized, message = e.message ?: "Invalid credentials")
                } catch (e: IllegalArgumentException) {
                    throw ApiException(HttpStatusCode.BadRequest, message = e.message ?: "Invalid login data")
                }
            }
        }
    }
}
