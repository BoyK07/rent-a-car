package dev.koenv.rentmycar.server.domain.service

import dev.koenv.rentmycar.server.storage.repository.CarPhotoRepositoryImpl
import dev.koenv.rentmycar.shared.domain.entity.CarPhoto
import kotlin.uuid.Uuid

class CarPhotoService(private val repo: CarPhotoRepositoryImpl) {
    suspend fun getAll(): List<CarPhoto> = repo.findAll()
    suspend fun getById(id: Uuid): CarPhoto? = repo.findById(id)
    suspend fun getByCarId(carId: Uuid): List<CarPhoto> = repo.findByCarId(carId)
    suspend fun create(photo: CarPhoto): CarPhoto = repo.create(photo)
    suspend fun update(id: Uuid, photo: CarPhoto): CarPhoto? = repo.update(id, photo)
    suspend fun delete(id: Uuid): Boolean = repo.delete(id)
}
