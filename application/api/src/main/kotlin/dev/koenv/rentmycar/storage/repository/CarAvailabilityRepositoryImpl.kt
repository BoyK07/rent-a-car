package dev.koenv.rentmycar.storage.repository

import dev.koenv.rentmycar.domain.entity.CarAvailability
import dev.koenv.rentmycar.domain.repository.CarAvailabilityRepository
import dev.koenv.rentmycar.plugins.dbQuery
import dev.koenv.rentmycar.storage.db.tables.CarAvailabilityTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import java.util.*

class CarAvailabilityRepositoryImpl : CarAvailabilityRepository {

    override suspend fun findAll(): List<CarAvailability> = dbQuery {
        CarAvailabilityTable.selectAll().map(::toEntity)
    }

    override suspend fun findById(id: UUID): CarAvailability? = dbQuery {
        CarAvailabilityTable.selectAll()
            .where { CarAvailabilityTable.id eq id }
            .mapNotNull(::toEntity)
            .singleOrNull()
    }

    override suspend fun existsById(id: UUID): Boolean = dbQuery {
        !CarAvailabilityTable.select(CarAvailabilityTable.id)
            .where { CarAvailabilityTable.id eq id }
            .empty()
    }

    override suspend fun create(entity: CarAvailability): CarAvailability = dbQuery {
        val insertedId = CarAvailabilityTable.insert {
            it[carId] = entity.carId
            it[startTime] = entity.startTime
            it[endTime] = entity.endTime
        } get CarAvailabilityTable.id

        CarAvailabilityTable.selectAll()
            .where { CarAvailabilityTable.id eq insertedId }
            .map(::toEntity)
            .single()
    }

    override suspend fun update(id: UUID, entity: CarAvailability): CarAvailability? = dbQuery {
        val updated = CarAvailabilityTable.update({ CarAvailabilityTable.id eq id }) {
            it[carId] = entity.carId
            it[startTime] = entity.startTime
            it[endTime] = entity.endTime
        }
        if (updated > 0)
            CarAvailabilityTable.selectAll()
                .where { CarAvailabilityTable.id eq id }
                .map(::toEntity)
                .singleOrNull()
        else null
    }

    override suspend fun delete(id: UUID): Boolean = dbQuery {
        CarAvailabilityTable.deleteWhere { CarAvailabilityTable.id eq id } > 0
    }

    override suspend fun findByCarId(carId: UUID): List<CarAvailability> = dbQuery {
        CarAvailabilityTable.selectAll()
            .where { CarAvailabilityTable.carId eq carId }
            .map(::toEntity)
    }

    private fun toEntity(row: ResultRow) = CarAvailability(
        id = row[CarAvailabilityTable.id],
        carId = row[CarAvailabilityTable.carId],
        startTime = row[CarAvailabilityTable.startTime],
        endTime = row[CarAvailabilityTable.endTime]
    )
}
