package dev.koenv.rentmycar.server.storage.repository

import dev.koenv.rentmycar.server.plugins.dbQuery
import dev.koenv.rentmycar.server.storage.db.tables.DrivingSessionsTable
import dev.koenv.rentmycar.shared.domain.entity.DrivingSession
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

class DrivingSessionRepositoryImpl {

    suspend fun findAll(): List<DrivingSession> = dbQuery {
        DrivingSessionsTable.selectAll().map(::toEntity)
    }

    suspend fun findById(id: Uuid): DrivingSession? = dbQuery {
        DrivingSessionsTable.selectAll().where { DrivingSessionsTable.id eq id.toJavaUuid() }.mapNotNull(::toEntity)
            .singleOrNull()
    }

    suspend fun existsById(id: Uuid): Boolean = dbQuery {
        !DrivingSessionsTable.select(DrivingSessionsTable.id).where { DrivingSessionsTable.id eq id.toJavaUuid() }
            .empty()
    }

    suspend fun create(entity: DrivingSession): DrivingSession = dbQuery {
        val insertedId = DrivingSessionsTable.insert {
            it[reservationId] = entity.reservationId.toJavaUuid()
            it[startTime] = entity.startTime
            it[endTime] = entity.endTime
            it[distanceKm] = entity.distanceKm
            it[harshAccelerations] = entity.harshAccelerations
            it[harshBrakes] = entity.harshBrakes
            it[recordedBy] = entity.recordedBy.toJavaUuid()
            it[createdAt] = entity.createdAt
        } get DrivingSessionsTable.id

        DrivingSessionsTable.selectAll().where { DrivingSessionsTable.id eq insertedId }.map(::toEntity).single()
    }

    suspend fun update(id: Uuid, entity: DrivingSession): DrivingSession? = dbQuery {
        val updated = DrivingSessionsTable.update({ DrivingSessionsTable.id eq id.toJavaUuid() }) {
            it[reservationId] = entity.reservationId.toJavaUuid()
            it[startTime] = entity.startTime
            it[endTime] = entity.endTime
            it[distanceKm] = entity.distanceKm
            it[harshAccelerations] = entity.harshAccelerations
            it[harshBrakes] = entity.harshBrakes
            it[recordedBy] = entity.recordedBy.toJavaUuid()
            it[createdAt] = entity.createdAt
        }
        if (updated > 0)
            DrivingSessionsTable.selectAll().where { DrivingSessionsTable.id eq id.toJavaUuid() }.map(::toEntity)
                .singleOrNull()
        else null
    }

    suspend fun delete(id: Uuid): Boolean = dbQuery {
        DrivingSessionsTable.deleteWhere { DrivingSessionsTable.id eq id.toJavaUuid() } > 0
    }

    suspend fun count(): Long = dbQuery { DrivingSessionsTable.selectAll().count() }

    suspend fun findByReservationId(reservationId: Uuid): List<DrivingSession> = dbQuery {
        DrivingSessionsTable.selectAll().where { DrivingSessionsTable.reservationId eq reservationId.toJavaUuid() }
            .map(::toEntity)
    }

    private fun toEntity(row: ResultRow) = DrivingSession(
        id = row[DrivingSessionsTable.id].toKotlinUuid(),
        reservationId = row[DrivingSessionsTable.reservationId].toKotlinUuid(),
        startTime = row[DrivingSessionsTable.startTime],
        endTime = row[DrivingSessionsTable.endTime],
        distanceKm = row[DrivingSessionsTable.distanceKm],
        harshAccelerations = row[DrivingSessionsTable.harshAccelerations],
        harshBrakes = row[DrivingSessionsTable.harshBrakes],
        recordedBy = row[DrivingSessionsTable.recordedBy].toKotlinUuid(),
        createdAt = row[DrivingSessionsTable.createdAt]
    )
}
