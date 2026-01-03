package dev.koenv.rentmycar.server.storage.db.tables

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object DrivingSessionsTable : Table("driving_sessions") {
    val id = uuid("id").autoGenerate()
    val reservationId = uuid("reservation_id").references(ReservationsTable.id)
    val startTime = datetime("start_time")
    val endTime = datetime("end_time")
    val distanceKm = double("distance_km")
    val harshAccelerations = integer("harsh_accelerations")
    val harshBrakes = integer("harsh_brakes")
    val recordedBy = uuid("recorded_by").references(UsersTable.id)
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}
