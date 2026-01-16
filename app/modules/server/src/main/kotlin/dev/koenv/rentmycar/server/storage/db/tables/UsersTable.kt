package dev.koenv.rentmycar.server.storage.db.tables

/**
 * Exposed table definition for users.
 * 
 * Columns:
 * - id: UUID primary key (auto-generated)
 * - name: User's display name
 * - email: Unique email address for authentication
 * - passwordHash: Argon2id hashed password
 * - role: User role (MEMBER, DRIVER, ADMIN)
 * - createdAt: Account creation timestamp (auto-set)
 */

import dev.koenv.rentmycar.shared.domain.enums.Role
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object UsersTable : Table("users") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role = enumerationByName("role", 20, Role::class)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
