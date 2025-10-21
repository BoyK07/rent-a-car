package dev.koenv.rentmycar.storage.repository

import dev.koenv.rentmycar.domain.entity.Car
import dev.koenv.rentmycar.domain.enums.CarCategory
import dev.koenv.rentmycar.domain.enums.FuelType
import dev.koenv.rentmycar.domain.repository.CarRepository
import dev.koenv.rentmycar.plugins.dbQuery
import dev.koenv.rentmycar.storage.db.tables.CarsTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import java.math.BigDecimal
import java.util.*

class CarRepositoryImpl : CarRepository {

    override suspend fun findAll(): List<Car> = dbQuery {
        CarsTable.selectAll().map(::toEntity)
    }

    override suspend fun findById(id: UUID): Car? = dbQuery {
        CarsTable.selectAll().where { CarsTable.id eq id }.mapNotNull(::toEntity).singleOrNull()
    }

    override suspend fun existsById(id: UUID): Boolean = dbQuery {
        !CarsTable.select(CarsTable.id).where { CarsTable.id eq id }.empty()
    }

    override suspend fun create(entity: Car): Car = dbQuery {
        val insertedId = CarsTable.insert {
            it[ownerId] = entity.ownerId
            it[make] = entity.make
            it[model] = entity.model
            it[category] = entity.category
            it[fuelType] = entity.fuelType
            it[ratePerHour] = entity.ratePerHour
            it[locationLat] = entity.locationLat
            it[locationLng] = entity.locationLng
            it[isActive] = entity.isActive
        } get CarsTable.id

        CarsTable.selectAll().where { CarsTable.id eq insertedId }.map(::toEntity).single()
    }

    override suspend fun update(id: UUID, entity: Car): Car? = dbQuery {
        val updated = CarsTable.update({ CarsTable.id eq id }) {
            it[ownerId] = entity.ownerId
            it[make] = entity.make
            it[model] = entity.model
            it[category] = entity.category
            it[fuelType] = entity.fuelType
            it[ratePerHour] = entity.ratePerHour
            it[locationLat] = entity.locationLat
            it[locationLng] = entity.locationLng
            it[isActive] = entity.isActive
        }
        if (updated > 0)
            CarsTable.selectAll().where { CarsTable.id eq id }.map(::toEntity).singleOrNull()
        else null
    }

    override suspend fun delete(id: UUID): Boolean = dbQuery {
        CarsTable.deleteWhere { CarsTable.id eq id } > 0
    }

    override suspend fun count(): Long = dbQuery { CarsTable.selectAll().count() }

    private fun toEntity(row: ResultRow) = Car(
        id = row[CarsTable.id],
        ownerId = row[CarsTable.ownerId],
        make = row[CarsTable.make],
        model = row[CarsTable.model],
        category = row[CarsTable.category],
        fuelType = row[CarsTable.fuelType],
        ratePerHour = row[CarsTable.ratePerHour],
        locationLat = row[CarsTable.locationLat],
        locationLng = row[CarsTable.locationLng],
        isActive = row[CarsTable.isActive]
    )
}
