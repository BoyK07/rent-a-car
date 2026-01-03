package dev.koenv.rentmycar.server.storage.repository

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toJavaBigDecimal
import dev.koenv.rentmycar.server.plugins.dbQuery
import dev.koenv.rentmycar.server.storage.db.tables.CarsTable
import dev.koenv.rentmycar.shared.domain.entity.Car
import dev.koenv.rentmycar.shared.domain.enums.CarCategory
import dev.koenv.rentmycar.shared.domain.enums.FuelType
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

class CarRepositoryImpl {

    suspend fun findAll(): List<Car> = dbQuery {
        CarsTable.selectAll().map(::toEntity)
    }

    suspend fun findById(id: Uuid): Car? = dbQuery {
        CarsTable.selectAll().where { CarsTable.id eq id.toJavaUuid() }.mapNotNull(::toEntity).singleOrNull()
    }

    suspend fun existsById(id: Uuid): Boolean = dbQuery {
        !CarsTable.select(CarsTable.id).where { CarsTable.id eq id.toJavaUuid() }.empty()
    }

    suspend fun create(entity: Car): Car = dbQuery {
        val insertedId = CarsTable.insert {
            it[ownerId] = entity.ownerId.toJavaUuid()
            it[brand] = entity.brand
            it[model] = entity.model
            it[category] = entity.category
            it[fuelType] = entity.fuelType
            it[ratePerHour] = entity.ratePerHour.toJavaBigDecimal()
            it[locationLat] = entity.locationLat
            it[locationLng] = entity.locationLng
            it[isActive] = entity.isActive
        } get CarsTable.id

        CarsTable.selectAll().where { CarsTable.id eq insertedId }.map(::toEntity).single()
    }

    suspend fun update(id: Uuid, entity: Car): Car? = dbQuery {
        val updated = CarsTable.update({ CarsTable.id eq id.toJavaUuid() }) {
            it[ownerId] = entity.ownerId.toJavaUuid()
            it[brand] = entity.brand
            it[model] = entity.model
            it[category] = entity.category
            it[fuelType] = entity.fuelType
            it[ratePerHour] = entity.ratePerHour.toJavaBigDecimal()
            it[locationLat] = entity.locationLat
            it[locationLng] = entity.locationLng
            it[isActive] = entity.isActive
        }
        if (updated > 0)
            CarsTable.selectAll().where { CarsTable.id eq id.toJavaUuid() }.map(::toEntity).singleOrNull()
        else null
    }

    suspend fun delete(id: Uuid): Boolean = dbQuery {
        CarsTable.deleteWhere { CarsTable.id eq id.toJavaUuid() } > 0
    }

    suspend fun count(): Long = dbQuery { CarsTable.selectAll().count() }

    suspend fun searchCars(
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
            query.andWhere { CarsTable.ratePerHour greaterEq minPrice.toJavaBigDecimal() }
        }
        if (maxPrice != null) {
            query.andWhere { CarsTable.ratePerHour lessEq maxPrice.toJavaBigDecimal() }
        }

        if (latitude != null && longitude != null && maxDistance != null) {
            val latDelta = maxDistance / 111.32
            val lngDelta = maxDistance / (111.32 * kotlin.math.cos(Math.toRadians(latitude)))
            val minLat = latitude - latDelta
            val maxLat = latitude + latDelta
            val minLng = longitude - lngDelta
            val maxLng = longitude + lngDelta

            query.andWhere { CarsTable.locationLat greaterEq minLat }
            query.andWhere { CarsTable.locationLat lessEq maxLat }
            query.andWhere { CarsTable.locationLng greaterEq minLng }
            query.andWhere { CarsTable.locationLng lessEq maxLng }
        }

        // Future geolocation optimization could go here with bounding-box math
        query.map(::toEntity)
    }

    suspend fun countSearchResults(
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
            query.andWhere { CarsTable.ratePerHour greaterEq minPrice.toJavaBigDecimal() }
        }
        if (maxPrice != null) {
            query.andWhere { CarsTable.ratePerHour lessEq maxPrice.toJavaBigDecimal() }
        }

        if (latitude != null && longitude != null && maxDistance != null) {
            val latDelta = maxDistance / 111.32
            val lngDelta = maxDistance / (111.32 * kotlin.math.cos(Math.toRadians(latitude)))
            val minLat = latitude - latDelta
            val maxLat = latitude + latDelta
            val minLng = longitude - lngDelta
            val maxLng = longitude + lngDelta

            query.andWhere { CarsTable.locationLat greaterEq minLat }
            query.andWhere { CarsTable.locationLat lessEq maxLat }
            query.andWhere { CarsTable.locationLng greaterEq minLng }
            query.andWhere { CarsTable.locationLng lessEq maxLng }
        }

        query.count().toInt()
    }

    suspend fun findNearbyCars(
        latitude: Double,
        longitude: Double,
        radius: Double,
        limit: Int
    ): List<Car> = dbQuery {
        val latDelta = radius / 111.32
        val lngDelta = radius / (111.32 * kotlin.math.cos(Math.toRadians(latitude)))
        val minLat = latitude - latDelta
        val maxLat = latitude + latDelta
        val minLng = longitude - lngDelta
        val maxLng = longitude + lngDelta

        CarsTable.selectAll()
            .andWhere { CarsTable.locationLat greaterEq minLat }
            .andWhere { CarsTable.locationLat lessEq maxLat }
            .andWhere { CarsTable.locationLng greaterEq minLng }
            .andWhere { CarsTable.locationLng lessEq maxLng }
            .limit(limit)
            .map(::toEntity)
    }

    private fun toEntity(row: ResultRow) = Car(
        id = row[CarsTable.id].toKotlinUuid(),
        ownerId = row[CarsTable.ownerId].toKotlinUuid(),
        brand = row[CarsTable.brand],
        model = row[CarsTable.model],
        category = row[CarsTable.category],
        fuelType = row[CarsTable.fuelType],
        ratePerHour = BigDecimal.parseString(row[CarsTable.ratePerHour].toString()),
        locationLat = row[CarsTable.locationLat],
        locationLng = row[CarsTable.locationLng],
        isActive = row[CarsTable.isActive]
    )
}
