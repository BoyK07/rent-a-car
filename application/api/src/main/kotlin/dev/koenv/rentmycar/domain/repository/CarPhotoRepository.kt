package dev.koenv.rentmycar.domain.repository

import dev.koenv.rentmycar.domain.entity.CarPhoto
import java.util.UUID

interface CarPhotoRepository : ReadRepository<CarPhoto, UUID>, WriteRepository<CarPhoto, UUID> {
    suspend fun findByCarId(carId: UUID): List<CarPhoto>
}