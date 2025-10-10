package dev.koenv.rentmycar.storage.repositories

import dev.koenv.rentmycar.domain.model.City
import dev.koenv.rentmycar.domain.repositories.CityRepository
import dev.koenv.rentmycar.storage.db.DatabaseFactory.dbQuery
import dev.koenv.rentmycar.storage.db.tables.CitiesTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class CityRepositoryImpl() : CityRepository {
    override suspend fun findAll(): List<City> = dbQuery {
        TODO("Not yet implemented")
    }

    override suspend fun findById(id: UUID): City? = dbQuery {
        TODO("Not yet implemented")
    }

    override suspend fun existsById(id: UUID): Boolean = dbQuery {
        TODO("Not yet implemented")
    }

    override suspend fun create(entity: City): City = dbQuery {
        TODO("Not yet implemented")
    }

    override suspend fun update(id: UUID, entity: City): City = dbQuery {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: UUID): Boolean = dbQuery {
        TODO("Not yet implemented")
    }

    override suspend fun count(): Long = dbQuery {
        TODO("Not yet implemented")
    }
}
