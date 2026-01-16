package dev.koenv.rentmycar.server.storage.db.tables

/**
 * Exposed table definition for reservations.
 * 
 * Columns:
 * - id: UUID primary key (auto-generated)
 * - carId: Foreign key to cars table
 * - renterId: Foreign key to users table
 * - startTime: Reservation start (datetime)
 * - endTime: Reservation end (datetime)
 * - status: Reservation status enum (PENDING, CONFIRMED, ACTIVE, COMPLETED, CANCELLED)
 * - priceTotal: Total price calculated server-side (decimal 10,2)
 * - pointsAwarded: Driving behavior points earned (default: 0)
 */

import dev.koenv.rentmycar.shared.domain.enums.ReservationStatus
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object ReservationsTable : Table("reservations") {
    val id = uuid("id").autoGenerate()
    val carId = uuid("car_id").references(CarsTable.id)
    val renterId = uuid("renter_id").references(UsersTable.id)
    val startTime = datetime("start_time")
    val endTime = datetime("end_time")
    val status = enumerationByName("status", 15, ReservationStatus::class)
    val priceTotal = decimal("price_total", 10, 2)
    val pointsAwarded = integer("points_awarded").default(0)

    override val primaryKey = PrimaryKey(id)
}
