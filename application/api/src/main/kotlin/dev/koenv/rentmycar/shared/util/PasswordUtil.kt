package dev.koenv.rentmycar.shared.util

import de.mkammerer.argon2.Argon2Factory

object PasswordUtil {
    private const val ITERATIONS = 6
    private const val MEMORY_KB = 262144 // 256 MB
    private const val PARALLELISM = 2

    private fun newArgon2() =
        Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id)

    fun hash(password: String): String {
        val argon2 = newArgon2()
        val pwd = password.toCharArray()
        try {
            return argon2.hash(ITERATIONS, MEMORY_KB, PARALLELISM, pwd)
        } finally {
            argon2.wipeArray(pwd)
        }
    }

    fun verify(password: String, hash: String): Boolean {
        val argon2 = newArgon2()
        val pwd = password.toCharArray()
        try {
            return runCatching { argon2.verify(hash, pwd) }.getOrDefault(false)
        } finally {
            argon2.wipeArray(pwd)
        }
    }
}
