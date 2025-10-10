package dev.koenv.rentmycar.storage.repository

import dev.koenv.rentmycar.domain.entity.City
import dev.koenv.rentmycar.domain.repository.CityRepository
import dev.koenv.rentmycar.storage.db.DatabaseFactory.dbQuery
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
