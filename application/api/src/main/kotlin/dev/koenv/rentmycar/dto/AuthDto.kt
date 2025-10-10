package dev.koenv.rentmycar.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(val token: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(val name: String, val age: Int, val email: String, val password: String)
