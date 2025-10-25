package dev.koenv.rentmycar.storage.repository

import dev.koenv.rentmycar.domain.entity.Car
import dev.koenv.rentmycar.domain.enums.CarCategory
import dev.koenv.rentmycar.domain.enums.FuelType
import dev.koenv.rentmycar.domain.repository.CarRepository
import dev.koenv.rentmycar.plugins.dbQuery
import dev.koenv.rentmycar.storage.db.tables.CarsTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.lowerCase
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
            it[brand] = entity.brand
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
            it[brand] = entity.brand
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

    override suspend fun searchCars(
        latitude: Double?,
        longitude: Double?,
        maxDistance: Double?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        category: CarCategory?,
        fuelType: FuelType?,
        brand: String?
    ): List<Car> = dbQuery {
        val query = CarsTable.selectAll()

        if (category != null) {
            query.andWhere { CarsTable.category eq category }
        }
        if (fuelType != null) {
            query.andWhere { CarsTable.fuelType eq fuelType }
        }
        if (!brand.isNullOrBlank()) {
            query.andWhere { CarsTable.brand.lowerCase() like "%${brand.lowercase()}%" }
        }
        if (minPrice != null) {
            query.andWhere { CarsTable.ratePerHour greaterEq minPrice }
        }
        if (maxPrice != null) {
            query.andWhere { CarsTable.ratePerHour lessEq maxPrice }
        }

        // Future geolocation optimization could go here with bounding-box math
        query.map(::toEntity)
    }

    override suspend fun countSearchResults(
        latitude: Double?,
        longitude: Double?,
        maxDistance: Double?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        category: CarCategory?,
        fuelType: FuelType?,
        brand: String?
    ): Int = dbQuery {
        val query = CarsTable.selectAll()

        if (category != null) {
            query.andWhere { CarsTable.category eq category }
        }
        if (fuelType != null) {
            query.andWhere { CarsTable.fuelType eq fuelType }
        }
        if (!brand.isNullOrBlank()) {
            query.andWhere { CarsTable.brand.lowerCase() like "%${brand.lowercase()}%" }
        }
        if (minPrice != null) {
            query.andWhere { CarsTable.ratePerHour greaterEq minPrice }
        }
        if (maxPrice != null) {
            query.andWhere { CarsTable.ratePerHour lessEq maxPrice }
        }

        query.count().toInt()
    }

    override suspend fun findNearbyCars(
        latitude: Double,
        longitude: Double,
        radius: Double,
        limit: Int
    ): List<Car> = dbQuery {
        // Voor nu: alle auto's ophalen en later filteren op afstand
        // TODO: Implementeer bounding box query voor betere performance
        CarsTable.selectAll().limit(limit).map(::toEntity)
    }

    private fun toEntity(row: ResultRow) = Car(
        id = row[CarsTable.id],
        ownerId = row[CarsTable.ownerId],
        brand = row[CarsTable.brand],
        model = row[CarsTable.model],
        category = row[CarsTable.category],
        fuelType = row[CarsTable.fuelType],
        ratePerHour = row[CarsTable.ratePerHour],
        locationLat = row[CarsTable.locationLat],
        locationLng = row[CarsTable.locationLng],
        isActive = row[CarsTable.isActive]
    )
}
