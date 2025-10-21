package dev.koenv.rentmycar.domain.service

import dev.koenv.rentmycar.domain.entity.CarPhoto
import dev.koenv.rentmycar.domain.repository.CarPhotoRepository
import java.util.UUID

class CarPhotoService(private val repo: CarPhotoRepository) {
    suspend fun getAll(): List<CarPhoto> = repo.findAll()
    suspend fun getById(id: UUID): CarPhoto? = repo.findById(id)
    suspend fun getByCarId(carId: UUID): List<CarPhoto> = repo.findByCarId(carId)
    suspend fun create(photo: CarPhoto): CarPhoto = repo.create(photo)
    suspend fun update(id: UUID, photo: CarPhoto): CarPhoto? = repo.update(id, photo)
    suspend fun delete(id: UUID): Boolean = repo.delete(id)
}
