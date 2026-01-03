package dev.koenv.rentmycar.server.storage.db.tables

import dev.koenv.rentmycar.shared.domain.enums.CarCategory
import dev.koenv.rentmycar.shared.domain.enums.FuelType
import org.jetbrains.exposed.v1.core.Table

object CarsTable : Table("cars") {
    val id = uuid("id").autoGenerate()
    val ownerId = uuid("owner_id").references(UsersTable.id)
    val brand = varchar("brand", 100)
    val model = varchar("model", 100)
    val category = enumerationByName("category", 10, CarCategory::class)
    val fuelType = enumerationByName("fuel_type", 10, FuelType::class).nullable()
    val ratePerHour = decimal("rate_per_hour", 10, 2)
    val locationLat = double("location_lat")
    val locationLng = double("location_lng")
    val isActive = bool("is_active").default(true)

    override val primaryKey = PrimaryKey(id)
}
