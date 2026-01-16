package dev.koenv.rentmycar.server.storage.db.tables

/**
 * Exposed table definition for car availability windows.
 * 
 * Columns:
 * - id: UUID primary key (auto-generated)
 * - carId: Foreign key to cars table
 * - startTime: Window start (datetime)
 * - endTime: Window end (datetime)
 * 
 * Defines time ranges when a car is available for booking.
 */

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object CarAvailabilityTable : Table("car_availability") {
    val id = uuid("id").autoGenerate()
    val carId = uuid("car_id").references(CarsTable.id)
    val startTime = datetime("start_time")
    val endTime = datetime("end_time")

    override val primaryKey = PrimaryKey(id)
}
