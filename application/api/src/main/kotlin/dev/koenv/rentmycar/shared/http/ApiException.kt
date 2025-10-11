// ApiException.kt
package dev.koenv.rentmycar.shared.http

import io.ktor.http.*

open class ApiException(
    val http: HttpStatusCode,
    val code: String,
    override val message: String,
    val details: Map<String, String>? = null
) : RuntimeException(message)
