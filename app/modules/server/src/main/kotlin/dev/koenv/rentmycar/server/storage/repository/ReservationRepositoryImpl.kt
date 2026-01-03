package dev.koenv.rentmycar.server.storage.repository

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toJavaBigDecimal
import dev.koenv.rentmycar.server.plugins.dbQuery
import dev.koenv.rentmycar.server.storage.db.tables.ReservationsTable
import dev.koenv.rentmycar.shared.domain.entity.Reservation
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

class ReservationRepositoryImpl {

    suspend fun findAll(): List<Reservation> = dbQuery {
        ReservationsTable.selectAll().map(::toEntity)
    }

    suspend fun findById(id: Uuid): Reservation? = dbQuery {
        ReservationsTable.selectAll()
            .where { ReservationsTable.id eq id.toJavaUuid() }
            .mapNotNull(::toEntity)
            .singleOrNull()
    }

    suspend fun existsById(id: Uuid): Boolean = dbQuery {
        !ReservationsTable.select(ReservationsTable.id)
            .where { ReservationsTable.id eq id.toJavaUuid() }
            .empty()
    }

    suspend fun create(entity: Reservation): Reservation = dbQuery {
        val insertedId = ReservationsTable.insert {
            it[carId] = entity.carId.toJavaUuid()
            it[renterId] = entity.renterId.toJavaUuid()
            it[startTime] = entity.startTime
            it[endTime] = entity.endTime
            it[status] = entity.status
            it[priceTotal] = entity.priceTotal.toJavaBigDecimal()
            it[pointsAwarded] = entity.pointsAwarded
        } get ReservationsTable.id

        ReservationsTable.selectAll()
            .where { ReservationsTable.id eq insertedId }
            .map(::toEntity)
            .single()
    }

    suspend fun update(id: Uuid, entity: Reservation): Reservation? = dbQuery {
        val updated = ReservationsTable.update({ ReservationsTable.id eq id.toJavaUuid() }) {
            it[carId] = entity.carId.toJavaUuid()
            it[renterId] = entity.renterId.toJavaUuid()
            it[startTime] = entity.startTime
            it[endTime] = entity.endTime
            it[status] = entity.status
            it[priceTotal] = entity.priceTotal.toJavaBigDecimal()
            it[pointsAwarded] = entity.pointsAwarded
        }
        if (updated > 0)
            ReservationsTable.selectAll()
                .where { ReservationsTable.id eq id.toJavaUuid() }
                .map(::toEntity)
                .singleOrNull()
        else null
    }

    suspend fun delete(id: Uuid): Boolean = dbQuery {
        ReservationsTable.deleteWhere { ReservationsTable.id eq id.toJavaUuid() } > 0
    }

    suspend fun count(): Long = dbQuery { ReservationsTable.selectAll().count() }

    suspend fun findByRenterId(renterId: Uuid): List<Reservation> = dbQuery {
        ReservationsTable.selectAll()
            .where { ReservationsTable.renterId eq renterId.toJavaUuid() }
            .map(::toEntity)
    }

    suspend fun findByCarId(carId: Uuid): List<Reservation> = dbQuery {
        ReservationsTable.selectAll()
            .where { ReservationsTable.carId eq carId.toJavaUuid() }
            .map(::toEntity)
    }

    suspend fun findByCarIdAndTimeRange(
        carId: Uuid,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): List<Reservation> = dbQuery {
        ReservationsTable.selectAll()
            .where {
                (ReservationsTable.carId eq carId.toJavaUuid()) and
                        (ReservationsTable.startTime lessEq endTime) and
                        (ReservationsTable.endTime greaterEq startTime)
            }
            .map(::toEntity)
    }

    private fun toEntity(row: ResultRow) = Reservation(
        id = row[ReservationsTable.id].toKotlinUuid(),
        carId = row[ReservationsTable.carId].toKotlinUuid(),
        renterId = row[ReservationsTable.renterId].toKotlinUuid(),
        startTime = row[ReservationsTable.startTime],
        endTime = row[ReservationsTable.endTime],
        status = row[ReservationsTable.status],
        priceTotal = BigDecimal.parseString(row[ReservationsTable.priceTotal].toPlainString()),
        pointsAwarded = row[ReservationsTable.pointsAwarded]
    )
}
