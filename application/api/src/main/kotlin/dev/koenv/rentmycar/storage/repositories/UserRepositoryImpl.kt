package dev.koenv.rentmycar.storage.repositories

import dev.koenv.rentmycar.domain.model.User
import dev.koenv.rentmycar.domain.repositories.UserRepository
import dev.koenv.rentmycar.storage.db.DatabaseFactory.dbQuery
import dev.koenv.rentmycar.storage.db.tables.UsersTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import java.util.UUID

class UserRepositoryImpl() : UserRepository {
    override suspend fun findAll(): List<User> = dbQuery {
        UsersTable.selectAll()
            .map { toDto(it) }
    }

    override suspend fun findById(id: UUID): User? = dbQuery {
        UsersTable.selectAll()
            .where(UsersTable.id eq id)
            .mapNotNull { toDto(it) }
            .singleOrNull()
    }

    override suspend fun existsById(id: UUID): Boolean = dbQuery {
        UsersTable.selectAll()
            .where(UsersTable.id eq id)
            .firstOrNull() != null
    }

    override suspend fun create(entity: User): User = dbQuery {
        UsersTable.insertReturning {
            it[name] = entity.name
            it[age] = entity.age
        }.mapNotNull { toDto(it) }.single()
    }

    override suspend fun update(id: UUID, entity: User): User = dbQuery {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: UUID): Boolean = dbQuery {
        TODO("Not yet implemented")
    }

    override suspend fun count(): Long = dbQuery {
        TODO("Not yet implemented")
    }

    private fun toDto(it: ResultRow) = User(
        id = it[UsersTable.id],
        name = it[UsersTable.name],
        age = it[UsersTable.age],
    )

}
