package dev.koenv.rentmycar.shared.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow

/**
 * Database manager for the RentMyCar application.
 * Provides access to database queries and handles database initialization.
 */
class DatabaseManager(driverFactory: DatabaseDriverFactory) {
    private val driver = driverFactory.createDriver()
    private val database = RentMyCarDatabase(driver)
    
    // Query accessors
    val carQueries = database.carQueries
    val userQueries = database.userQueries
    val reservationQueries = database.reservationQueries
    
    /**
     * Provides a Flow of all cars from the database.
     */
    fun getAllCarsFlow(): Flow<List<Car>> {
        return carQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }
    
    /**
     * Provides a Flow of all active cars from the database.
     */
    fun getActiveCarsFlow(): Flow<List<Car>> {
        return carQueries.selectActive()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }
    
    /**
     * Provides a Flow of a specific car by ID.
     */
    fun getCarByIdFlow(carId: String): Flow<Car?> {
        return carQueries.selectById(carId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
    }
    
    /**
     * Provides a Flow of all users from the database.
     */
    fun getAllUsersFlow(): Flow<List<User>> {
        return userQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }
    
    /**
     * Provides a Flow of a specific user by ID.
     */
    fun getUserByIdFlow(userId: String): Flow<User?> {
        return userQueries.selectById(userId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
    }
    
    /**
     * Provides a Flow of all reservations from the database.
     */
    fun getAllReservationsFlow(): Flow<List<Reservation>> {
        return reservationQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }
    
    /**
     * Provides a Flow of active reservations (PENDING or CONFIRMED).
     */
    fun getActiveReservationsFlow(): Flow<List<Reservation>> {
        return reservationQueries.selectActive()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }
    
    /**
     * Provides a Flow of reservations for a specific renter.
     */
    fun getReservationsByRenterFlow(renterId: String): Flow<List<Reservation>> {
        return reservationQueries.selectByRenter(renterId)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }
    
    /**
     * Provides a Flow of a specific reservation by ID.
     */
    fun getReservationByIdFlow(reservationId: String): Flow<Reservation?> {
        return reservationQueries.selectById(reservationId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
    }
    
    /**
     * Executes a block of code within a database transaction.
     * Provides access to database transaction for DAOs.
     */
    fun transaction(body: () -> Unit) {
        database.transaction {
            body()
        }
    }
    
    /**
     * Clears all data from the database.
     * Useful for logout or data reset.
     */
    fun clearAllData() {
        database.transaction {
            carQueries.deleteAll()
            userQueries.deleteAll()
            reservationQueries.deleteAll()
        }
    }
    
    /**
     * Closes the database connection.
     */
    fun close() {
        driver.close()
    }
}
