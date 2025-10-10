package dev.koenv.rentmycar.domain.entity

import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    USER,
    ADMIN
}
