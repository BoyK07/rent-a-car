package dev.koenv.rentmycar.storage.repository

import dev.koenv.rentmycar.domain.entity.DrivingSession
import dev.koenv.rentmycar.domain.repository.DrivingSessionRepository
import dev.koenv.rentmycar.plugins.dbQuery
import dev.koenv.rentmycar.storage.db.tables.DrivingSessionsTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import java.util.*

class DrivingSessionRepositoryImpl : DrivingSessionRepository {

    override suspend fun findAll(): List<DrivingSession> = dbQuery {
        DrivingSessionsTable.selectAll().map(::toEntity)
    }

    override suspend fun findById(id: UUID): DrivingSession? = dbQuery {
        DrivingSessionsTable.selectAll().where { DrivingSessionsTable.id eq id }.mapNotNull(::toEntity).singleOrNull()
    }

    override suspend fun existsById(id: UUID): Boolean = dbQuery {
        !DrivingSessionsTable.select(DrivingSessionsTable.id).where { DrivingSessionsTable.id eq id }.empty()
    }

    override suspend fun create(entity: DrivingSession): DrivingSession = dbQuery {
        val insertedId = DrivingSessionsTable.insert {
            it[reservationId] = entity.reservationId
            it[distanceKm] = entity.distanceKm
            it[harshAccelerations] = entity.harshAccelerations
            it[harshBrakes] = entity.harshBrakes
        } get DrivingSessionsTable.id

        DrivingSessionsTable.selectAll().where { DrivingSessionsTable.id eq insertedId }.map(::toEntity).single()
    }

    override suspend fun update(id: UUID, entity: DrivingSession): DrivingSession? = dbQuery {
        val updated = DrivingSessionsTable.update({ DrivingSessionsTable.id eq id }) {
            it[reservationId] = entity.reservationId
            it[distanceKm] = entity.distanceKm
            it[harshAccelerations] = entity.harshAccelerations
            it[harshBrakes] = entity.harshBrakes
        }
        if (updated > 0)
            DrivingSessionsTable.selectAll().where { DrivingSessionsTable.id eq id }.map(::toEntity).singleOrNull()
        else null
    }

    override suspend fun delete(id: UUID): Boolean = dbQuery {
        DrivingSessionsTable.deleteWhere { DrivingSessionsTable.id eq id } > 0
    }

    override suspend fun count(): Long = dbQuery { DrivingSessionsTable.selectAll().count() }

    override suspend fun findByReservationId(reservationId: UUID): List<DrivingSession> = dbQuery {
        DrivingSessionsTable.selectAll().where { DrivingSessionsTable.reservationId eq reservationId }.map(::toEntity)
    }

    private fun toEntity(row: ResultRow) = DrivingSession(
        id = row[DrivingSessionsTable.id],
        reservationId = row[DrivingSessionsTable.reservationId],
        distanceKm = row[DrivingSessionsTable.distanceKm],
        harshAccelerations = row[DrivingSessionsTable.harshAccelerations],
        harshBrakes = row[DrivingSessionsTable.harshBrakes]
    )
}
