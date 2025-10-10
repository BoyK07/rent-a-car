package dev.koenv.rentmycar.domain.repositories

interface Repository<T, ID> : ReadRepository<T, ID>, WriteRepository<T, ID>, CountingRepository

interface ReadRepository<T, ID> {
    suspend fun findAll(): List<T>
    suspend fun findById(id: ID): T?
    suspend fun existsById(id: ID): Boolean
}

interface WriteRepository<T, ID> {
    suspend fun create(entity: T): T
    suspend fun update(id: ID, entity: T): T?
    suspend fun delete(id: ID): Boolean
}

interface CountingRepository {
    suspend fun count(): Long
}
