package dev.koenv.rentmycar.domain.services

import dev.koenv.rentmycar.domain.model.City
import dev.koenv.rentmycar.domain.repositories.CityRepository

class CityService(private val repo: CityRepository) {
    suspend fun create(city: City): Int = repo.create(city)
    suspend fun read(id: Int): City? = repo.findById(id)
    suspend fun update(id: Int, city: City): Boolean = repo.update(id, city)
    suspend fun delete(id: Int): Boolean = repo.delete(id)
}
