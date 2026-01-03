package dev.koenv.rentmycar.server.storage.repository

import dev.koenv.rentmycar.server.plugins.dbQuery
import dev.koenv.rentmycar.server.storage.db.tables.CarPhotosTable
import dev.koenv.rentmycar.shared.domain.entity.CarPhoto
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

class CarPhotoRepositoryImpl {

    suspend fun findAll(): List<CarPhoto> = dbQuery {
        CarPhotosTable.selectAll().map(::toEntity)
    }

    suspend fun findById(id: Uuid): CarPhoto? = dbQuery {
        CarPhotosTable.selectAll().where { CarPhotosTable.id eq id.toJavaUuid() }.mapNotNull(::toEntity).singleOrNull()
    }

    suspend fun existsById(id: Uuid): Boolean = dbQuery {
        !CarPhotosTable.select(CarPhotosTable.id).where { CarPhotosTable.id eq id.toJavaUuid() }.empty()
    }

    suspend fun create(entity: CarPhoto): CarPhoto = dbQuery {
        val insertedId = CarPhotosTable.insert {
            it[carId] = entity.carId.toJavaUuid()
            it[url] = entity.url
            it[isPrimary] = entity.isPrimary
        } get CarPhotosTable.id

        CarPhotosTable.selectAll().where { CarPhotosTable.id eq insertedId }.map(::toEntity).single()
    }

    suspend fun update(id: Uuid, entity: CarPhoto): CarPhoto? = dbQuery {
        val updated = CarPhotosTable.update({ CarPhotosTable.id eq id.toJavaUuid() }) {
            it[carId] = entity.carId.toJavaUuid()
            it[url] = entity.url
            it[isPrimary] = entity.isPrimary
        }
        if (updated > 0)
            CarPhotosTable.selectAll().where { CarPhotosTable.id eq id.toJavaUuid() }.map(::toEntity).singleOrNull()
        else null
    }

    suspend fun delete(id: Uuid): Boolean = dbQuery {
        CarPhotosTable.deleteWhere { CarPhotosTable.id eq id.toJavaUuid() } > 0
    }

    suspend fun findByCarId(carId: Uuid): List<CarPhoto> = dbQuery {
        CarPhotosTable.selectAll().where { CarPhotosTable.carId eq carId.toJavaUuid() }.map(::toEntity)
    }

    private fun toEntity(row: ResultRow) = CarPhoto(
        id = row[CarPhotosTable.id].toKotlinUuid(),
        carId = row[CarPhotosTable.carId].toKotlinUuid(),
        url = row[CarPhotosTable.url],
        isPrimary = row[CarPhotosTable.isPrimary]
    )
}
