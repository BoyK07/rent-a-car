package dev.koenv.rentmycar.storage.db.tables

import org.jetbrains.exposed.sql.Table

object CitiesTable : Table("cities") {
    val id = uuid("id").autoGenerate() // UUID primary key
    val name = varchar("name", length = 255)
    val population = integer("population")

    override val primaryKey = PrimaryKey(id)
}
