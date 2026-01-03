package dev.koenv.rentmycar.server.storage.repository

import dev.koenv.rentmycar.server.plugins.dbQuery
import dev.koenv.rentmycar.server.storage.db.tables.CarAvailabilityTable
import dev.koenv.rentmycar.shared.domain.entity.CarAvailability
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

class CarAvailabilityRepositoryImpl {

    suspend fun findAll(): List<CarAvailability> = dbQuery {
        CarAvailabilityTable.selectAll().map(::toEntity)
    }

    suspend fun findById(id: Uuid): CarAvailability? = dbQuery {
        CarAvailabilityTable.selectAll()
            .where { CarAvailabilityTable.id eq id.toJavaUuid() }
            .mapNotNull(::toEntity)
            .singleOrNull()
    }

    suspend fun existsById(id: Uuid): Boolean = dbQuery {
        !CarAvailabilityTable.select(CarAvailabilityTable.id)
            .where { CarAvailabilityTable.id eq id.toJavaUuid() }
            .empty()
    }

    suspend fun create(entity: CarAvailability): CarAvailability = dbQuery {
        val insertedId = CarAvailabilityTable.insert {
            it[carId] = entity.carId.toJavaUuid()
            it[startTime] = entity.startTime
            it[endTime] = entity.endTime
        } get CarAvailabilityTable.id

        CarAvailabilityTable.selectAll()
            .where { CarAvailabilityTable.id eq insertedId }
            .map(::toEntity)
            .single()
    }

    suspend fun update(id: Uuid, entity: CarAvailability): CarAvailability? = dbQuery {
        val updated = CarAvailabilityTable.update({ CarAvailabilityTable.id eq id.toJavaUuid() }) {
            it[carId] = entity.carId.toJavaUuid()
            it[startTime] = entity.startTime
            it[endTime] = entity.endTime
        }
        if (updated > 0)
            CarAvailabilityTable.selectAll()
                .where { CarAvailabilityTable.id eq id.toJavaUuid() }
                .map(::toEntity)
                .singleOrNull()
        else null
    }

    suspend fun delete(id: Uuid): Boolean = dbQuery {
        CarAvailabilityTable.deleteWhere { CarAvailabilityTable.id eq id.toJavaUuid() } > 0
    }

    suspend fun findByCarId(carId: Uuid): List<CarAvailability> = dbQuery {
        CarAvailabilityTable.selectAll()
            .where { CarAvailabilityTable.carId eq carId.toJavaUuid() }
            .map(::toEntity)
    }

    private fun toEntity(row: ResultRow) = CarAvailability(
        id = row[CarAvailabilityTable.id].toKotlinUuid(),
        carId = row[CarAvailabilityTable.carId].toKotlinUuid(),
        startTime = row[CarAvailabilityTable.startTime],
        endTime = row[CarAvailabilityTable.endTime]
    )
}
