package dev.koenv.rentmycar.server.storage.repository

import dev.koenv.rentmycar.server.plugins.dbQuery
import dev.koenv.rentmycar.server.storage.db.tables.UsersTable
import dev.koenv.rentmycar.shared.domain.entity.User
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import java.util.UUID
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

/**
 * Repository implementation for User entity CRUD operations.
 * 
 * Uses Exposed ORM with UsersTable for database access.
 * All database operations are executed asynchronously via dbQuery.
 * 
 * Provides:
 * - CRUD operations (create, read, update, delete)
 * - Email lookup for authentication
 * - Existence checks for validation
 */
class UserRepositoryImpl {

    suspend fun findAll(): List<User> = dbQuery {
        UsersTable.selectAll().map(::toEntity)
    }

    suspend fun findById(id: Uuid): User? = dbQuery {
        UsersTable.selectAll().where { UsersTable.id eq id.toJavaUuid() }.mapNotNull(::toEntity).singleOrNull()
    }

    suspend fun existsById(id: Uuid): Boolean = dbQuery {
        !UsersTable.select(UsersTable.id).where { UsersTable.id eq id.toJavaUuid() }.empty()
    }

    suspend fun findByEmail(email: String): User? = dbQuery {
        UsersTable.selectAll().where { UsersTable.email eq email }.mapNotNull(::toEntity).singleOrNull()
    }

    suspend fun create(entity: User): User = dbQuery {
        val insertedId = UsersTable.insert {
            it[name] = entity.name
            it[email] = entity.email
            it[passwordHash] = entity.passwordHash
            it[role] = entity.role
        } get UsersTable.id

        UsersTable.selectAll().where { UsersTable.id eq insertedId }.map(::toEntity).single()
    }

    suspend fun update(id: Uuid, entity: User): User? = dbQuery {
        val updated = UsersTable.update({ UsersTable.id eq id.toJavaUuid() }) {
            it[name] = entity.name
            it[email] = entity.email
            it[passwordHash] = entity.passwordHash
            it[role] = entity.role
        }
        if (updated > 0)
            UsersTable.selectAll().where { UsersTable.id eq id.toJavaUuid() }.map(::toEntity).singleOrNull()
        else null
    }

    suspend fun delete(id: Uuid): Boolean = dbQuery {
        val javaUuid = UUID.fromString(id.toString())
        UsersTable.deleteWhere { UsersTable.id eq javaUuid } > 0
    }

    suspend fun count(): Long = dbQuery { UsersTable.selectAll().count() }

    private fun toEntity(row: ResultRow) = User(
        id = row[UsersTable.id].toKotlinUuid(),
        name = row[UsersTable.name],
        email = row[UsersTable.email],
        passwordHash = row[UsersTable.passwordHash],
        role = row[UsersTable.role],
        createdAt = row[UsersTable.createdAt]
    )
}
