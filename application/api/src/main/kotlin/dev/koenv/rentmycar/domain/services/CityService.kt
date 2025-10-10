package dev.koenv.rentmycar.domain.services

import dev.koenv.rentmycar.domain.model.City
import dev.koenv.rentmycar.domain.repositories.CityRepository
import java.util.UUID

class CityService(private val repo: CityRepository) {
    suspend fun create(city: City): City = repo.create(city)
    suspend fun read(id: UUID): City? = repo.findById(id)
    suspend fun update(id: UUID, city: City): City? = repo.update(id, city)
    suspend fun delete(id: UUID): Boolean = repo.delete(id)
}
