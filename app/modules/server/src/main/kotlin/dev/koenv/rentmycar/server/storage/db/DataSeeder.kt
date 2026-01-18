package dev.koenv.rentmycar.server.storage.db

import de.mkammerer.argon2.Argon2Factory
import dev.koenv.rentmycar.server.plugins.dbQuery
import dev.koenv.rentmycar.server.storage.db.tables.*
import dev.koenv.rentmycar.shared.domain.enums.*
import io.ktor.server.application.*
import kotlinx.datetime.*
import org.jetbrains.exposed.v1.jdbc.*
import java.math.BigDecimal
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid
import java.util.UUID as JavaUUID

/**
 * Seeds the database with test data for development.
 * 
 * Creates:
 * - 3 users (admin, driver/owner, member/renter)
 * - 3 cars with different categories and fuel types
 * - Car photos for each car
 * - Availability windows for cars
 * - Sample reservations in different states
 * - Sample driving sessions with telemetry data
 * 
 * Only runs if the database is empty (no users exist).
 * Safe to call multiple times - will skip if data already exists.
 */
@OptIn(ExperimentalUuidApi::class)
suspend fun seedData() {
    // Check if data already exists
    val userCount = dbQuery {
        UsersTable.selectAll().count()
    }
    
    if (userCount > 0) {
        println("Data already exists (found $userCount users), skipping seed.")
        return
    }
    
    println("Seeding data...")
    
    val argon2 = Argon2Factory.create()
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    
    // Create users and get their IDs
    val (adminUserId, ownerUserId, renterUserId) = dbQuery {
        val adminUserId = UsersTable.insert {
            it[name] = "Admin User"
            it[email] = "admin@rentmycar.dev"
            it[passwordHash] = argon2.hash(10, 65536, 1, "admin123".toCharArray())
            it[role] = Role.ADMIN
        } get UsersTable.id
        
        val ownerUserId = UsersTable.insert {
            it[name] = "John Doe"
            it[email] = "john.doe@example.com"
            it[passwordHash] = argon2.hash(10, 65536, 1, "driver123".toCharArray())
            it[role] = Role.DRIVER
        } get UsersTable.id
        
        val renterUserId = UsersTable.insert {
            it[name] = "Jane Smith"
            it[email] = "jane.smith@example.com"
            it[passwordHash] = argon2.hash(10, 65536, 1, "member123".toCharArray())
            it[role] = Role.MEMBER
        } get UsersTable.id
        
        Triple(adminUserId, ownerUserId, renterUserId)
    }
    
    // Create cars and get their IDs
    val (car1Id, car2Id, car3Id) = dbQuery {
        val car1Id = CarsTable.insert {
            it[CarsTable.ownerId] = ownerUserId
            it[brand] = "Toyota"
            it[model] = "Camry"
            it[category] = CarCategory.ICE
            it[fuelType] = FuelType.PETROL
            it[ratePerHour] = BigDecimal("15.50")
            it[addressLine1] = "Coolsingel 1"
            it[postalCode] = "3012 AA"
            it[city] = "Rotterdam"
            it[country] = "Netherlands"
            it[formattedAddress] = "Coolsingel 1, 3012 AA Rotterdam, Netherlands"
            it[locationLat] = 51.9225 // Rotterdam
            it[locationLng] = 4.47917
            it[isActive] = true
        } get CarsTable.id
        
        val car2Id = CarsTable.insert {
            it[CarsTable.ownerId] = ownerUserId
            it[brand] = "Tesla"
            it[model] = "Model Y"
            it[category] = CarCategory.BEV
            it[fuelType] = FuelType.ELECTRIC
            it[ratePerHour] = BigDecimal("25.00")
            it[addressLine1] = "Dam 1"
            it[postalCode] = "1012 JS"
            it[city] = "Amsterdam"
            it[country] = "Netherlands"
            it[formattedAddress] = "Dam 1, 1012 JS Amsterdam, Netherlands"
            it[locationLat] = 52.3676 // Amsterdam
            it[locationLng] = 4.9041
            it[isActive] = true
        } get CarsTable.id
        
        val car3Id = CarsTable.insert {
            it[CarsTable.ownerId] = ownerUserId
            it[brand] = "Toyota"
            it[model] = "Mirai"
            it[category] = CarCategory.FCEV
            it[fuelType] = null // FCEV typically uses hydrogen, not traditional fuel
            it[ratePerHour] = BigDecimal("30.00")
            it[addressLine1] = "Vredenburg 1"
            it[postalCode] = "3511 BA"
            it[city] = "Utrecht"
            it[country] = "Netherlands"
            it[formattedAddress] = "Vredenburg 1, 3511 BA Utrecht, Netherlands"
            it[locationLat] = 52.0907 // Utrecht
            it[locationLng] = 5.1214
            it[isActive] = true
        } get CarsTable.id
        
        Triple(car1Id, car2Id, car3Id)
    }
    
    // Create car photos
    dbQuery {
        // Car 1 photos
        CarPhotosTable.insert {
            it[CarPhotosTable.carId] = car1Id
            it[url] = "https://example.com/photos/camry-front.jpg"
            it[isPrimary] = true
        }
        CarPhotosTable.insert {
            it[CarPhotosTable.carId] = car1Id
            it[url] = "https://example.com/photos/camry-interior.jpg"
            it[isPrimary] = false
        }
        
        // Car 2 photos
        CarPhotosTable.insert {
            it[CarPhotosTable.carId] = car2Id
            it[url] = "https://example.com/photos/modely-front.jpg"
            it[isPrimary] = true
        }
        CarPhotosTable.insert {
            it[CarPhotosTable.carId] = car2Id
            it[url] = "https://example.com/photos/modely-charging.jpg"
            it[isPrimary] = false
        }
        
        // Car 3 photos
        CarPhotosTable.insert {
            it[CarPhotosTable.carId] = car3Id
            it[url] = "https://example.com/photos/mirai-front.jpg"
            it[isPrimary] = true
        }
    }
    
    // Create availability windows for the next 7 days
    dbQuery {
        for (dayOffset in 0..6) {
            val day = now.date.plus(dayOffset, DateTimeUnit.DAY)
            val startTime = LocalDateTime(day, LocalTime(8, 0))
            val endTime = LocalDateTime(day, LocalTime(20, 0))
            
            // Make all cars available
            for (carId in listOf(car1Id, car2Id, car3Id)) {
                CarAvailabilityTable.insert {
                    it[CarAvailabilityTable.carId] = carId
                    it[CarAvailabilityTable.startTime] = startTime
                    it[CarAvailabilityTable.endTime] = endTime
                }
            }
        }
    }
    
    // Create sample reservations and get their IDs
    val (reservation1Id, reservation2Id, reservation3Id) = dbQuery {
        // Confirmed reservation (tomorrow)
        val tomorrow = now.date.plus(1, DateTimeUnit.DAY)
        val reservation1Id = ReservationsTable.insert {
            it[ReservationsTable.carId] = car1Id
            it[ReservationsTable.renterId] = renterUserId
            it[startTime] = LocalDateTime(tomorrow, LocalTime(10, 0))
            it[endTime] = LocalDateTime(tomorrow, LocalTime(14, 0))
            it[status] = ReservationStatus.CONFIRMED
            it[priceTotal] = BigDecimal("62.00") // 4 hours * 15.50
            it[pointsAwarded] = 0
        } get ReservationsTable.id
        
        // Completed reservation (yesterday)
        val yesterday = now.date.minus(1, DateTimeUnit.DAY)
        val reservation2Id = ReservationsTable.insert {
            it[ReservationsTable.carId] = car2Id
            it[ReservationsTable.renterId] = renterUserId
            it[startTime] = LocalDateTime(yesterday, LocalTime(9, 0))
            it[endTime] = LocalDateTime(yesterday, LocalTime(17, 0))
            it[status] = ReservationStatus.COMPLETED
            it[priceTotal] = BigDecimal("200.00") // 8 hours * 25.00
            it[pointsAwarded] = 85 // Good driving
        } get ReservationsTable.id
        
        // Cancelled reservation
        val reservation3Id = ReservationsTable.insert {
            it[ReservationsTable.carId] = car3Id
            it[ReservationsTable.renterId] = renterUserId
            it[startTime] = LocalDateTime(now.date, LocalTime(15, 0))
            it[endTime] = LocalDateTime(now.date, LocalTime(18, 0))
            it[status] = ReservationStatus.CANCELLED
            it[priceTotal] = BigDecimal("90.00") // 3 hours * 30.00
            it[pointsAwarded] = 0
        } get ReservationsTable.id
        
        Triple(reservation1Id, reservation2Id, reservation3Id)
    }
    
    // Create driving session for completed reservation
    dbQuery {
        val yesterday = now.date.minus(1, DateTimeUnit.DAY)
        DrivingSessionsTable.insert {
            it[reservationId] = reservation2Id
            it[startTime] = LocalDateTime(yesterday, LocalTime(9, 0))
            it[endTime] = LocalDateTime(yesterday, LocalTime(17, 0))
            it[distanceKm] = 156.5
            it[harshAccelerations] = 2
            it[harshBrakes] = 3
            it[recordedBy] = renterUserId
            it[createdAt] = LocalDateTime(yesterday, LocalTime(17, 5))
        }
    }
    
    argon2.wipeArray(argon2.hash(10, 65536, 1, "".toCharArray()).toCharArray())
    
    println("Data seeded successfully!")
    println("Users created:")
    println("  - admin@rentmycar.dev (password: admin123) - Role: ADMIN")
    println("  - john.doe@example.com (password: driver123) - Role: DRIVER")
    println("  - jane.smith@example.com (password: member123) - Role: MEMBER")
    println("Cars created: 3 (Toyota Camry, Tesla Model Y, Toyota Mirai)")
    println("Reservations created: 3 (confirmed, completed, cancelled)")
    println("Driving sessions created: 1")
}
