package dev.koenv.rentmycar.shared.domain.enums

import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    ADMIN,
    DRIVER,
    MEMBER;

    /**
     * Returns true if this role includes the permissions of [other].
     * Used for role inheritance (e.g. DRIVER includes MEMBER permissions).
     */
    fun includes(other: Role): Boolean {
        return when (this) {
            ADMIN -> true
            DRIVER -> other == MEMBER || other == DRIVER
            MEMBER -> other == MEMBER
        }
    }

    /**
     * Returns true if the given [Role] can be self-assigned during registration.
     */
    companion object {
        fun isRegisterable(role: Role): Boolean {
            return role == MEMBER || role == DRIVER
        }
    }
}
