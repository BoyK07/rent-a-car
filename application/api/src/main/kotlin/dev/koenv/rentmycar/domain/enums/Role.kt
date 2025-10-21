package dev.koenv.rentmycar.domain.enums

import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    ADMIN,
    DRIVER,
    SUPPORT,
}