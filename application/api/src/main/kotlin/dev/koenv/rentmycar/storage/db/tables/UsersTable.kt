package dev.koenv.rentmycar.storage.db.tables

import dev.koenv.rentmycar.domain.entity.Role
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.*

object UsersTable : Table("users") {
    val id = uuid("id").autoGenerate()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role = enumerationByName("role", 20, Role::class)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
