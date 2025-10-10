package dev.koenv.rentmycar.shared.util

import de.mkammerer.argon2.Argon2Factory

object PasswordUtil {
    private const val ITERATIONS = 3
    private const val MEMORY_KB = 65536 // 64 MB
    private const val PARALLELISM = 1

    fun hash(password: String): String {
        val argon2 = Argon2Factory.create()
        val pwd = password.toCharArray()
        try {
            return argon2.hash(ITERATIONS, MEMORY_KB, PARALLELISM, pwd)
        } finally {
            argon2.wipeArray(pwd)
        }
    }

    fun verify(password: String, hash: String): Boolean {
        val argon2 = Argon2Factory.create()
        val pwd = password.toCharArray()
        try {
            return try {
                argon2.verify(hash, pwd)
            } catch (e: Exception) {
                false
            }
        } finally {
            argon2.wipeArray(pwd)
        }
    }
}
