package dev.koenv.rentmycar.shared.db.dao

import dev.koenv.rentmycar.shared.db.DatabaseManager
import dev.koenv.rentmycar.shared.db.User
import dev.koenv.rentmycar.shared.domain.enums.Role
import dev.koenv.rentmycar.shared.dto.user.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.uuid.Uuid

/**
 * Data Access Object for User operations.
 * Handles conversion between UserDto and database User entity.
 */
class UserDao(private val databaseManager: DatabaseManager) {
    
    /**
     * Get all users as a Flow.
     */
    fun getAllUsersFlow(): Flow<List<UserDto>> {
        return databaseManager.getAllUsersFlow().map { users -> users.map { it.toDto() } }
    }
    
    /**
     * Get user by ID as a Flow.
     */
    fun getUserByIdFlow(userId: Uuid): Flow<UserDto?> {
        return databaseManager.getUserByIdFlow(userId.toString()).map { user -> user?.toDto() }
    }
    
    /**
     * Get all users synchronously.
     */
    fun getAllUsers(): List<UserDto> {
        return databaseManager.userQueries.selectAll().executeAsList().map { it.toDto() }
    }
    
    /**
     * Get user by ID synchronously.
     */
    fun getUserById(userId: Uuid): UserDto? {
        return databaseManager.userQueries.selectById(userId.toString()).executeAsOneOrNull()?.toDto()
    }
    
    /**
     * Get user by email.
     */
    fun getUserByEmail(email: String): UserDto? {
        return databaseManager.userQueries.selectByEmail(email).executeAsOneOrNull()?.toDto()
    }
    
    /**
     * Get users by role.
     */
    fun getUsersByRole(role: Role): List<UserDto> {
        return databaseManager.userQueries.selectByRole(role.name).executeAsList().map { it.toDto() }
    }
    
    /**
     * Insert or update a user.
     */
    fun insertOrUpdate(userDto: UserDto) {
        val now = Clock.System.now().toEpochMilliseconds()
        databaseManager.userQueries.insertOrReplace(
            id = userDto.id.toString(),
            name = userDto.name,
            email = userDto.email,
            role = userDto.role.name,
            createdAt = now,
            updatedAt = now
        )
    }
    
    /**
     * Insert or update multiple users.
     */
    fun insertOrUpdateAll(users: List<UserDto>) {
        databaseManager.transaction {
            users.forEach { insertOrUpdate(it) }
        }
    }
    
    /**
     * Delete user by ID.
     */
    fun deleteById(userId: Uuid) {
        databaseManager.userQueries.deleteById(userId.toString())
    }
    
    /**
     * Delete all users.
     */
    fun deleteAll() {
        databaseManager.userQueries.deleteAll()
    }
    
    /**
     * Count all users.
     */
    fun countAll(): Long {
        return databaseManager.userQueries.countAll().executeAsOne()
    }
    
    private fun User.toDto(): UserDto {
        return UserDto(
            id = Uuid.parse(this.id),
            name = this.name,
            email = this.email,
            role = Role.valueOf(this.role)
        )
    }
}
