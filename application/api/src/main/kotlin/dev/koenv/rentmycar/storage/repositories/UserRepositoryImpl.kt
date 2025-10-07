package dev.koenv.rentmycar.storage.repositories

import dev.koenv.rentmycar.domain.model.User
import dev.koenv.rentmycar.domain.repositories.UserRepository
import dev.koenv.rentmycar.storage.db.tables.UsersTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class UserRepositoryImpl(private val db: Database) : UserRepository {

    private suspend fun <T> dbQuery(block: () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, db) { block() }

    override suspend fun create(user: User): Int = dbQuery {
        UsersTable.insert {
            it[name] = user.name
            it[age] = user.age
        }[UsersTable.id]
    }

    override suspend fun findById(id: Int): User? = dbQuery {
        UsersTable
            .select(UsersTable.id eq id)
            .map { row -> User(row[UsersTable.id], row[UsersTable.name], row[UsersTable.age]) }
            .singleOrNull()
    }

    override suspend fun update(id: Int, user: User): Boolean = dbQuery {
        UsersTable.update({ UsersTable.id eq id }) {
            it[name] = user.name
            it[age] = user.age
        } > 0
    }

    override suspend fun delete(id: Int): Boolean = dbQuery {
        UsersTable.deleteWhere(op = { UsersTable.id eq id }) > 0
    }
}
