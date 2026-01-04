package dev.koenv.rentmycar.server.routes.api.v1.auth

import dev.koenv.rentmycar.server.domain.service.AuthService
import dev.koenv.rentmycar.server.routes.RouteRegistrar
import dev.koenv.rentmycar.server.util.requireBodyOrFail
import dev.koenv.rentmycar.server.util.respondCreated
import dev.koenv.rentmycar.server.util.respondSuccess
import dev.koenv.rentmycar.server.util.respondError
import dev.koenv.rentmycar.shared.dto.auth.LoginRequestDto
import dev.koenv.rentmycar.shared.dto.auth.RegisterRequestDto
import dev.koenv.rentmycar.shared.resources.ApiV1
import io.ktor.http.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.resources.post
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

object AuthRoutes : RouteRegistrar {
    override fun Route.register() {
        val authService by inject<AuthService>()

        post<ApiV1.Auth.Register> {
            val req = call.requireBodyOrFail<RegisterRequestDto>()
            try {
                val response = authService.register(req)
                call.respondCreated(response)
            } catch (e: IllegalStateException) {
                call.respondError(
                    status = HttpStatusCode.Conflict,
                    message = e.message ?: "Registration failed",
                    code = "REGISTRATION_CONFLICT",
                    traceId = call.callId
                )
            } catch (e: IllegalArgumentException) {
                call.respondError(
                    status = HttpStatusCode.BadRequest,
                    message = e.message ?: "Invalid registration data",
                    code = "INVALID_REGISTRATION_DATA",
                    traceId = call.callId
                )
            }
        }

        post<ApiV1.Auth.Login> {
            val req = call.requireBodyOrFail<LoginRequestDto>()
            try {
                val response = authService.login(req)
                call.respondSuccess(response)
            } catch (e: IllegalStateException) {
                call.respondError(
                    status = HttpStatusCode.Unauthorized,
                    message = e.message ?: "Invalid credentials",
                    code = "INVALID_CREDENTIALS",
                    traceId = call.callId
                )
            } catch (e: IllegalArgumentException) {
                call.respondError(
                    status = HttpStatusCode.BadRequest,
                    message = e.message ?: "Invalid login data",
                    code = "INVALID_LOGIN_DATA",
                    traceId = call.callId
                )
            }
        }
    }
}
