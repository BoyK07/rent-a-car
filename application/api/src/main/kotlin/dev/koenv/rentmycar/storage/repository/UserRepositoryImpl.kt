package dev.koenv.rentmycar.storage.repository

import dev.koenv.rentmycar.domain.entity.User
import dev.koenv.rentmycar.domain.repository.UserRepository
import dev.koenv.rentmycar.plugins.dbQuery
import dev.koenv.rentmycar.storage.db.tables.UsersTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import java.util.UUID

class UserRepositoryImpl : UserRepository {

    override suspend fun findAll(): List<User> = dbQuery {
        UsersTable.selectAll().map(::toEntity)
    }

    override suspend fun findById(id: UUID): User? = dbQuery {
        UsersTable.selectAll()
            .where { UsersTable.id eq id }
            .mapNotNull(::toEntity)
            .singleOrNull()
    }

    override suspend fun existsById(id: UUID): Boolean = dbQuery {
        !UsersTable.select(UsersTable.id).where { UsersTable.id eq id }.empty()
    }

    override suspend fun findByEmail(email: String): User? = dbQuery {
        UsersTable.selectAll()
            .where { UsersTable.email eq email }
            .mapNotNull(::toEntity)
            .singleOrNull()
    }

    override suspend fun create(entity: User): User = dbQuery {
        val insertedId = UsersTable.insert {
            it[name] = entity.name
            it[email] = entity.email
            it[passwordHash] = entity.passwordHash
            it[role] = entity.role
        } get UsersTable.id

        UsersTable.selectAll()
            .where { UsersTable.id eq insertedId }
            .map(::toEntity)
            .single()
    }

    override suspend fun update(id: UUID, entity: User): User? = dbQuery {
        val updated = UsersTable.update({ UsersTable.id eq id }) {
            it[name] = entity.name
            it[email] = entity.email
            it[passwordHash] = entity.passwordHash
            it[role] = entity.role
        }
        if (updated > 0) {
            UsersTable.selectAll()
                .where { UsersTable.id eq id }
                .map(::toEntity)
                .singleOrNull()
        } else null
    }

    override suspend fun delete(id: UUID): Boolean = dbQuery {
        UsersTable.deleteWhere { UsersTable.id eq id } > 0
    }

    override suspend fun count(): Long = dbQuery {
        UsersTable.selectAll().count()
    }

    private fun toEntity(row: ResultRow): User = User(
        id = row[UsersTable.id],
        name = row[UsersTable.name],
        email = row[UsersTable.email],
        passwordHash = row[UsersTable.passwordHash],
        role = row[UsersTable.role],
        createdAt = row[UsersTable.createdAt]
    )
}
