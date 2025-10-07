package dev.koenv.rentmycar.storage.repositories

import dev.koenv.rentmycar.domain.model.City
import dev.koenv.rentmycar.domain.repositories.CityRepository
import dev.koenv.rentmycar.storage.db.tables.CitiesTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class CityRepositoryImpl(private val db: Database) : CityRepository {

    private suspend fun <T> dbQuery(block: () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, db) { block() }

    override suspend fun create(city: City): Int = dbQuery {
        CitiesTable.insert {
            it[name] = city.name
            it[population] = city.population
        }[CitiesTable.id]
    }

    override suspend fun findById(id: Int): City? = dbQuery {
        CitiesTable
            .select(CitiesTable.id eq id)
            .map { row -> City(row[CitiesTable.id], row[CitiesTable.name], row[CitiesTable.population]) }
            .singleOrNull()
    }

    override suspend fun update(id: Int, city: City): Boolean = dbQuery {
        CitiesTable.update({ CitiesTable.id eq id }) {
            it[name] = city.name
            it[population] = city.population
        } > 0
    }

    override suspend fun delete(id: Int): Boolean = dbQuery {
        CitiesTable.deleteWhere(op = { CitiesTable.id eq id }) > 0
    }
}
