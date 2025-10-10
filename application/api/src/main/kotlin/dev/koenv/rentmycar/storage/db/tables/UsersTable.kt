package dev.koenv.rentmycar.storage.db.tables

import dev.koenv.rentmycar.domain.entity.Role
import org.jetbrains.exposed.v1.core.Table

object UsersTable : Table("users") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 50)
    val age = integer("age")
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role = enumerationByName("role", 20, Role::class)

    override val primaryKey = PrimaryKey(id)
}
