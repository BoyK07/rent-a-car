package dev.koenv.rentmycar.server.storage.db.tables

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
