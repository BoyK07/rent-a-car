package dev.koenv.rentmycar.server.util

import de.mkammerer.argon2.Argon2Factory

/**
 * Utility for secure password hashing and verification using Argon2id.
 * 
 * Argon2id is a memory-hard password hashing function that is resistant to:
 * - GPU cracking attacks
 * - Side-channel attacks
 * - Time-memory trade-off attacks
 * 
 * Configuration:
 * - Algorithm: Argon2id (hybrid of Argon2i and Argon2d)
 * - Iterations: 6
 * - Memory: 256 MB
 * - Parallelism: 2 threads
 * 
 * These settings provide strong security while maintaining reasonable performance.
 */
object PasswordUtil {
    private const val ITERATIONS = 6
    private const val MEMORY_KB = 262144 // 256 MB
    private const val PARALLELISM = 2

    private fun newArgon2() =
        Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id)

    /**
     * Hashes a plain text password using Argon2id.
     * 
     * The password is securely wiped from memory after hashing.
     * 
     * @param password The plain text password to hash
     * @return The hashed password string (includes salt and parameters)
     */
    fun hash(password: String): String {
        val argon2 = newArgon2()
        val pwd = password.toCharArray()
        try {
            return argon2.hash(ITERATIONS, MEMORY_KB, PARALLELISM, pwd)
        } finally {
            argon2.wipeArray(pwd)
        }
    }

    /**
     * Verifies a password against a hash.
     * 
     * The password is securely wiped from memory after verification.
     * Returns false if verification fails or if an error occurs.
     * 
     * @param password The plain text password to verify
     * @param hash The hashed password to check against
     * @return true if password matches hash, false otherwise
     */
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
