package dev.koenv.rentmycar.storage.repository

import dev.koenv.rentmycar.domain.entity.Reservation
import dev.koenv.rentmycar.domain.repository.ReservationRepository
import dev.koenv.rentmycar.plugins.dbQuery
import dev.koenv.rentmycar.storage.db.tables.ReservationsTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import java.util.*

class ReservationRepositoryImpl : ReservationRepository {

    override suspend fun findAll(): List<Reservation> = dbQuery {
        ReservationsTable.selectAll().map(::toEntity)
    }

    override suspend fun findById(id: UUID): Reservation? = dbQuery {
        ReservationsTable.selectAll()
            .where { ReservationsTable.id eq id }
            .mapNotNull(::toEntity)
            .singleOrNull()
    }

    override suspend fun existsById(id: UUID): Boolean = dbQuery {
        !ReservationsTable.select(ReservationsTable.id)
            .where { ReservationsTable.id eq id }
            .empty()
    }

    override suspend fun create(entity: Reservation): Reservation = dbQuery {
        val insertedId = ReservationsTable.insert {
            it[carId] = entity.carId
            it[renterId] = entity.renterId
            it[startTime] = entity.startTime
            it[endTime] = entity.endTime
            it[status] = entity.status
            it[priceTotal] = entity.priceTotal
            it[pointsAwarded] = entity.pointsAwarded
        } get ReservationsTable.id

        ReservationsTable.selectAll()
            .where { ReservationsTable.id eq insertedId }
            .map(::toEntity)
            .single()
    }

    override suspend fun update(id: UUID, entity: Reservation): Reservation? = dbQuery {
        val updated = ReservationsTable.update({ ReservationsTable.id eq id }) {
            it[carId] = entity.carId
            it[renterId] = entity.renterId
            it[startTime] = entity.startTime
            it[endTime] = entity.endTime
            it[status] = entity.status
            it[priceTotal] = entity.priceTotal
            it[pointsAwarded] = entity.pointsAwarded
        }
        if (updated > 0)
            ReservationsTable.selectAll()
                .where { ReservationsTable.id eq id }
                .map(::toEntity)
                .singleOrNull()
        else null
    }

    override suspend fun delete(id: UUID): Boolean = dbQuery {
        ReservationsTable.deleteWhere { ReservationsTable.id eq id } > 0
    }

    override suspend fun count(): Long = dbQuery { ReservationsTable.selectAll().count() }

    override suspend fun findByRenterId(renterId: UUID): List<Reservation> = dbQuery {
        ReservationsTable.selectAll()
            .where { ReservationsTable.renterId eq renterId }
            .map(::toEntity)
    }

    private fun toEntity(row: ResultRow) = Reservation(
        id = row[ReservationsTable.id],
        carId = row[ReservationsTable.carId],
        renterId = row[ReservationsTable.renterId],
        startTime = row[ReservationsTable.startTime],
        endTime = row[ReservationsTable.endTime],
        status = row[ReservationsTable.status],
        priceTotal = row[ReservationsTable.priceTotal],
        pointsAwarded = row[ReservationsTable.pointsAwarded]
    )
}
