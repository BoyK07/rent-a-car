package dev.koenv.rentmycar.dto

import dev.koenv.rentmycar.domain.entity.Role
import dev.koenv.rentmycar.domain.entity.User
import dev.koenv.rentmycar.shared.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class UserResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val name: String,
    val age: Int,
    val email: String,
    val role: Role
)

fun User.toResponse() = UserResponse(
    id = this.id!!,
    name = this.name,
    age = this.age,
    email = this.email,
    role = this.role
)