package dev.koenv.rentmycar.storage.repository

import dev.koenv.rentmycar.domain.entity.CarPhoto
import dev.koenv.rentmycar.domain.repository.CarPhotoRepository
import dev.koenv.rentmycar.plugins.dbQuery
import dev.koenv.rentmycar.storage.db.tables.CarPhotosTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import java.util.*

class CarPhotoRepositoryImpl : CarPhotoRepository {

    override suspend fun findAll(): List<CarPhoto> = dbQuery {
        CarPhotosTable.selectAll().map(::toEntity)
    }

    override suspend fun findById(id: UUID): CarPhoto? = dbQuery {
        CarPhotosTable.selectAll().where { CarPhotosTable.id eq id }.mapNotNull(::toEntity).singleOrNull()
    }

    override suspend fun existsById(id: UUID): Boolean = dbQuery {
        !CarPhotosTable.select(CarPhotosTable.id).where { CarPhotosTable.id eq id }.empty()
    }

    override suspend fun create(entity: CarPhoto): CarPhoto = dbQuery {
        val insertedId = CarPhotosTable.insert {
            it[carId] = entity.carId
            it[url] = entity.url
            it[isPrimary] = entity.isPrimary
        } get CarPhotosTable.id

        CarPhotosTable.selectAll().where { CarPhotosTable.id eq insertedId }.map(::toEntity).single()
    }

    override suspend fun update(id: UUID, entity: CarPhoto): CarPhoto? = dbQuery {
        val updated = CarPhotosTable.update({ CarPhotosTable.id eq id }) {
            it[carId] = entity.carId
            it[url] = entity.url
            it[isPrimary] = entity.isPrimary
        }
        if (updated > 0)
            CarPhotosTable.selectAll().where { CarPhotosTable.id eq id }.map(::toEntity).singleOrNull()
        else null
    }

    override suspend fun delete(id: UUID): Boolean = dbQuery {
        CarPhotosTable.deleteWhere { CarPhotosTable.id eq id } > 0
    }

    override suspend fun findByCarId(carId: UUID): List<CarPhoto> = dbQuery {
        CarPhotosTable.selectAll().where { CarPhotosTable.carId eq carId }.map(::toEntity)
    }

    private fun toEntity(row: ResultRow) = CarPhoto(
        id = row[CarPhotosTable.id],
        carId = row[CarPhotosTable.carId],
        url = row[CarPhotosTable.url],
        isPrimary = row[CarPhotosTable.isPrimary]
    )
}
