package dev.koenv.rentmycar.domain.entity

import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    ADMIN,
    DRIVER,
    SUPPORT // placeholder for possible future system role
}
