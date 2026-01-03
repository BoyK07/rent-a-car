package dev.koenv.rentmycar.server.storage.db.tables

import org.jetbrains.exposed.v1.core.Table

object CarPhotosTable : Table("car_photos") {
    val id = uuid("id").autoGenerate()
    val carId = uuid("car_id").references(CarsTable.id)
    val url = varchar("url", 512)
    val isPrimary = bool("is_primary").default(false)

    override val primaryKey = PrimaryKey(id)
}
