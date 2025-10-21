package dev.koenv.rentmycar.storage.db.tables

import org.jetbrains.exposed.v1.core.Table

object DrivingSessionsTable : Table("driving_sessions") {
    val id = uuid("id").autoGenerate()
    val reservationId = uuid("reservation_id").references(ReservationsTable.id)
    val distanceKm = decimal("distance_km", 8, 2)
    val harshAccelerations = integer("harsh_accelerations")
    val harshBrakes = integer("harsh_brakes")

    override val primaryKey = PrimaryKey(id)
}
