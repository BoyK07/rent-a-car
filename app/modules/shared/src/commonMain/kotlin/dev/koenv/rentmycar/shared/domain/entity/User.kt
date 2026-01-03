package dev.koenv.rentmycar.shared.domain.entity

import dev.koenv.rentmycar.shared.domain.enums.Role
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class User(
    val id: Uuid? = null,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: Role = Role.MEMBER,
    val createdAt: LocalDateTime? = null
)
