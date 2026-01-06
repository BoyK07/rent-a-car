package dev.koenv.rentmycar.shared.db.dao

import dev.koenv.rentmycar.shared.db.DatabaseManager
import dev.koenv.rentmycar.shared.db.Reservation
import dev.koenv.rentmycar.shared.domain.enums.ReservationStatus
import dev.koenv.rentmycar.shared.dto.reservation.ReservationDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlin.uuid.Uuid

/**
 * Data Access Object for Reservation operations.
 * Handles conversion between ReservationDto and database Reservation entity.
 */
class ReservationDao(private val databaseManager: DatabaseManager) {
    
    /**
     * Get all reservations as a Flow.
     */
    fun getAllReservationsFlow(): Flow<List<ReservationDto>> {
        return databaseManager.getAllReservationsFlow().map { reservations -> reservations.map { it.toDto() } }
    }
    
    /**
     * Get active reservations as a Flow.
     */
    fun getActiveReservationsFlow(): Flow<List<ReservationDto>> {
        return databaseManager.getActiveReservationsFlow().map { reservations -> reservations.map { it.toDto() } }
    }
    
    /**
     * Get reservations by renter as a Flow.
     */
    fun getReservationsByRenterFlow(renterId: Uuid): Flow<List<ReservationDto>> {
        return databaseManager.getReservationsByRenterFlow(renterId.toString()).map { reservations -> 
            reservations.map { it.toDto() } 
        }
    }
    
    /**
     * Get reservation by ID as a Flow.
     */
    fun getReservationByIdFlow(reservationId: Uuid): Flow<ReservationDto?> {
        return databaseManager.getReservationByIdFlow(reservationId.toString()).map { reservation -> 
            reservation?.toDto() 
        }
    }
    
    /**
     * Get all reservations synchronously.
     */
    fun getAllReservations(): List<ReservationDto> {
        return databaseManager.reservationQueries.selectAll().executeAsList().map { it.toDto() }
    }
    
    /**
     * Get reservation by ID synchronously.
     */
    fun getReservationById(reservationId: Uuid): ReservationDto? {
        return databaseManager.reservationQueries.selectById(reservationId.toString()).executeAsOneOrNull()?.toDto()
    }
    
    /**
     * Get reservations by car.
     */
    fun getReservationsByCar(carId: Uuid): List<ReservationDto> {
        return databaseManager.reservationQueries.selectByCar(carId.toString()).executeAsList().map { it.toDto() }
    }
    
    /**
     * Get reservations by renter.
     */
    fun getReservationsByRenter(renterId: Uuid): List<ReservationDto> {
        return databaseManager.reservationQueries.selectByRenter(renterId.toString()).executeAsList().map { it.toDto() }
    }
    
    /**
     * Get reservations by status.
     */
    fun getReservationsByStatus(status: ReservationStatus): List<ReservationDto> {
        return databaseManager.reservationQueries.selectByStatus(status.name).executeAsList().map { it.toDto() }
    }
    
    /**
     * Insert or update a reservation.
     */
    fun insertOrUpdate(reservationDto: ReservationDto) {
        val now = Clock.System.now().toEpochMilliseconds()
        databaseManager.reservationQueries.insertOrReplace(
            id = reservationDto.id.toString(),
            carId = reservationDto.carId.toString(),
            renterId = reservationDto.renterId.toString(),
            startTime = reservationDto.startTime.toString(),
            endTime = reservationDto.endTime.toString(),
            status = reservationDto.status.name,
            priceTotal = reservationDto.priceTotal.toString(),
            pointsAwarded = reservationDto.pointsAwarded?.toLong(),
            createdAt = now,
            updatedAt = now
        )
    }
    
    /**
     * Insert or update multiple reservations.
     */
    fun insertOrUpdateAll(reservations: List<ReservationDto>) {
        databaseManager.transaction {
            reservations.forEach { insertOrUpdate(it) }
        }
    }
    
    /**
     * Update reservation status.
     */
    fun updateStatus(reservationId: Uuid, status: ReservationStatus) {
        val now = Clock.System.now().toEpochMilliseconds()
        databaseManager.reservationQueries.updateStatus(
            status = status.name,
            updatedAt = now,
            id = reservationId.toString()
        )
    }
    
    /**
     * Delete reservation by ID.
     */
    fun deleteById(reservationId: Uuid) {
        databaseManager.reservationQueries.deleteById(reservationId.toString())
    }
    
    /**
     * Delete all reservations.
     */
    fun deleteAll() {
        databaseManager.reservationQueries.deleteAll()
    }
    
    /**
     * Count all reservations.
     */
    fun countAll(): Long {
        return databaseManager.reservationQueries.countAll().executeAsOne()
    }
    
    /**
     * Count reservations by status.
     */
    fun countByStatus(status: ReservationStatus): Long {
        return databaseManager.reservationQueries.countByStatus(status.name).executeAsOne()
    }
    
    private fun Reservation.toDto(): ReservationDto {
        return ReservationDto(
            id = Uuid.parse(this.id),
            carId = Uuid.parse(this.carId),
            renterId = Uuid.parse(this.renterId),
            startTime = LocalDateTime.parse(this.startTime),
            endTime = LocalDateTime.parse(this.endTime),
            status = ReservationStatus.valueOf(this.status),
            priceTotal = com.ionspin.kotlin.bignum.decimal.BigDecimal.parseString(this.priceTotal),
            pointsAwarded = this.pointsAwarded?.toInt() ?: 0
        )
    }
}
