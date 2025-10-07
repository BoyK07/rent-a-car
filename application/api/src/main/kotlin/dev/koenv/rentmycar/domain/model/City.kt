package dev.koenv.rentmycar.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class City(
    val id: Int? = null,
    val name: String,
    val population: Int
)
