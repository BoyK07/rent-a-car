package dev.koenv.rentmycar.server.storage.db.tables

/**
 * Exposed table definition for cars.
 * 
 * Columns:
 * - id: UUID primary key (auto-generated)
 * - ownerId: Foreign key to users table
 * - brand: Car manufacturer
 * - model: Car model name
 * - category: Car category enum (SEDAN, SUV, etc.)
 * - fuelType: Fuel type enum (GASOLINE, DIESEL, ELECTRIC, HYBRID) - nullable
 * - ratePerHour: Rental rate in currency per hour (decimal 10,2)
 * - addressLine1: Street address
 * - addressLine2: Optional unit/suite
 * - postalCode: Postal code
 * - city: City
 * - country: Country
 * - formattedAddress: Provider formatted address
 * - locationLat: Latitude coordinate
 * - locationLng: Longitude coordinate
 * - isActive: Whether car is available for rental (default: true)
 */

import dev.koenv.rentmycar.shared.domain.enums.CarCategory
import dev.koenv.rentmycar.shared.domain.enums.FuelType
import org.jetbrains.exposed.v1.core.Table

object CarsTable : Table("cars") {
    val id = uuid("id").autoGenerate()
    val ownerId = uuid("owner_id").references(UsersTable.id)
    val brand = varchar("brand", 100)
    val model = varchar("model", 100)
    val category = enumerationByName("category", 10, CarCategory::class)
    val fuelType = enumerationByName("fuel_type", 10, FuelType::class).nullable()
    val ratePerHour = decimal("rate_per_hour", 10, 2)
    val addressLine1 = varchar("address_line1", 255).nullable()
    val addressLine2 = varchar("address_line2", 255).nullable()
    val postalCode = varchar("postal_code", 32).nullable()
    val city = varchar("city", 120).nullable()
    val country = varchar("country", 120).nullable()
    val formattedAddress = varchar("formatted_address", 255).nullable()
    val locationLat = double("location_lat")
    val locationLng = double("location_lng")
    val isActive = bool("is_active").default(true)

    override val primaryKey = PrimaryKey(id)
}
