package dev.koenv.rentmycar.domain.repositories

import dev.koenv.rentmycar.domain.model.City

interface CityRepository {
    suspend fun create(city: City): Int
    suspend fun findById(id: Int): City?
    suspend fun update(id: Int, city: City): Boolean
    suspend fun delete(id: Int): Boolean
}
