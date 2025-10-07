package dev.koenv.rentmycar.storage.db.tables

import org.jetbrains.exposed.sql.Table

object UsersTable : Table("users") {
    val id = uuid("id").autoGenerate() // UUID primary key
    val name = varchar("name", length = 50)
    val age = integer("age")

    override val primaryKey = PrimaryKey(id)
}
