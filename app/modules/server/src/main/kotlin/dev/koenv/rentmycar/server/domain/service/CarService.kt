package dev.koenv.rentmycar.server.domain.service

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import dev.koenv.rentmycar.server.storage.repository.CarRepositoryImpl
import dev.koenv.rentmycar.shared.domain.entity.Car
import dev.koenv.rentmycar.shared.domain.enums.CarCategory
import dev.koenv.rentmycar.shared.domain.enums.FuelType
import kotlin.uuid.Uuid

/**
 * Service layer for car management and cost calculations.
 * 
 * Provides business logic for:
 * - Car CRUD operations
 * - Filtering and querying cars
 * - Total Cost of Ownership (TCO) calculations
 * - Cost per kilometer calculations
 * 
 * TCO calculations include energy costs, maintenance, and fixed overheads
 * based on vehicle category and fuel type.
 * 
 * @property repo The car repository for data persistence
 */
class CarService(private val repo: CarRepositoryImpl) {
    
    /**
     * Decimal precision mode for all financial calculations.
     * Uses 10 decimal places with half-away-from-zero rounding.
     */
    private val decimalMode = DecimalMode(
        decimalPrecision = 10,
        roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO
    )

    // CRUD Operations
    
    suspend fun getAll(): List<Car> = repo.findAll()
    
    suspend fun getById(id: Uuid): Car? = repo.findById(id)
    
    suspend fun create(car: Car): Car = repo.create(car)
    
    suspend fun update(id: Uuid, car: Car): Car? = repo.update(id, car)
    
    suspend fun delete(id: Uuid): Boolean = repo.delete(id)
    
    suspend fun count(): Long = repo.count()

    /**
     * Filters cars based on multiple criteria.
     * 
     * All filter parameters are optional. When null, that filter is ignored.
     * Filtering is done in-memory using sequence operations for efficiency.
     * 
     * @param ownerId Filter by car owner's UUID
     * @param category Filter by vehicle category (ICE, BEV, FCEV)
     * @param fuelType Filter by fuel type (PETROL, DIESEL, etc.)
     * @param isActive Filter by availability status
     * @param maxRate Filter by maximum hourly rate
     * @return List of cars matching all specified criteria
     */
    suspend fun listFiltered(
        ownerId: Uuid? = null,
        category: CarCategory? = null,
        fuelType: FuelType? = null,
        isActive: Boolean? = null,
        maxRate: BigDecimal? = null
    ): List<Car> {
        return repo.findAll().asSequence()
            .filter { ownerId == null || it.ownerId == ownerId }
            .filter { category == null || it.category == category }
            .filter { fuelType == null || it.fuelType == fuelType }
            .filter { isActive == null || it.isActive == isActive }
            .filter { maxRate == null || it.ratePerHour <= maxRate }
            .toList()
    }

    /**
     * Calculates the Total Cost of Ownership (TCO) per year for a vehicle.
     * 
     * TCO includes:
     * - Variable costs: energy consumption and maintenance based on annual kilometers
     * - Fixed costs: insurance, tax, parking (category-dependent estimates)
     * 
     * Assumptions:
     * - ICE vehicles: €1000/year fixed costs
     * - BEV vehicles: €800/year fixed costs (lower maintenance, insurance)
     * - FCEV vehicles: €1200/year fixed costs (higher due to technology)
     * 
     * @param car The vehicle to calculate TCO for
     * @param annualKm Expected annual kilometers driven (default: 15000)
     * @return Annual TCO in euros, rounded to 2 decimal places
     */
    fun calculateTcoPerYear(car: Car, annualKm: Int = 15000): BigDecimal {
        // Variable costs per kilometer
        val variableCostPerKm = calculateCostPerKm(car)

        // Fixed annual overheads based on vehicle category
        val fixedOverhead = when (car.category) {
            CarCategory.ICE -> BigDecimal.parseString("1000.00")
            CarCategory.BEV -> BigDecimal.parseString("800.00")
            CarCategory.FCEV -> BigDecimal.parseString("1200.00")
        }

        // Total TCO = (variable cost/km × annual km) + fixed costs
        val annualKmDecimal = BigDecimal.fromInt(annualKm)
        val distanceComponent = variableCostPerKm.multiply(annualKmDecimal, decimalMode)
        return distanceComponent.add(fixedOverhead, decimalMode)
            .roundToDigitPositionAfterDecimalPoint(2, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
    }

    /**
     * Calculates the cost per kilometer for a vehicle.
     * 
     * Includes:
     * - Energy costs (fuel or electricity) based on consumption rates
     * - Maintenance costs (tires, wear, servicing)
     * 
     * Energy price assumptions (€):
     * - Petrol: €1.90/L
     * - Diesel: €1.70/L
     * - LPG: €1.05/L
     * - Electricity: €0.30/kWh
     * 
     * Consumption assumptions:
     * - Petrol/Diesel: 6-7 L/100km
     * - Electric: 18 kWh/100km
     * - Hybrid: 5 L/100km
     * 
     * Maintenance assumptions (€/km):
     * - BEV: €0.04 (lower due to fewer moving parts)
     * - ICE: €0.06 (traditional engine maintenance)
     * - FCEV: €0.07 (specialized maintenance requirements)
     * 
     * @param car The vehicle to calculate costs for
     * @return Cost per kilometer in euros, rounded to 4 decimal places
     */
    fun calculateCostPerKm(car: Car): BigDecimal {
        // Current energy prices
        val petrolPricePerLiter = BigDecimal.parseString("1.90")
        val dieselPricePerLiter = BigDecimal.parseString("1.70")
        val lpgPricePerLiter = BigDecimal.parseString("1.05")
        val electricityPricePerKwh = BigDecimal.parseString("0.30")

        // Fuel consumption per 100km
        val litersPer100Km = when (car.fuelType) {
            FuelType.PETROL -> BigDecimal.parseString("7.0")
            FuelType.DIESEL -> BigDecimal.parseString("6.0")
            FuelType.LPG -> BigDecimal.parseString("8.0")
            FuelType.HYBRID -> BigDecimal.parseString("5.0")
            FuelType.ELECTRIC -> BigDecimal.ZERO
            null -> when (car.category) {
                CarCategory.BEV -> BigDecimal.ZERO
                else -> BigDecimal.parseString("7.0")
            }
        }

        // Electric consumption per 100km
        val kwhPer100Km = when (car.category) {
            CarCategory.BEV -> BigDecimal.parseString("18.0")
            CarCategory.ICE -> BigDecimal.ZERO
            CarCategory.FCEV -> BigDecimal.ONE // Placeholder for H2 equivalent
        }

        val hundred = BigDecimal.parseString("100.0")

        // Calculate energy cost per kilometer
        val energyCostPerKm: BigDecimal = when (car.category) {
            CarCategory.BEV -> {
                // Electric vehicles: (kWh/100km × price/kWh) ÷ 100
                kwhPer100Km.multiply(electricityPricePerKwh, decimalMode)
                    .divide(hundred, decimalMode)
            }

            CarCategory.ICE -> {
                // Combustion engines: (L/100km × price/L) ÷ 100
                val pricePerLiter = when (car.fuelType) {
                    FuelType.PETROL -> petrolPricePerLiter
                    FuelType.DIESEL -> dieselPricePerLiter
                    FuelType.LPG -> lpgPricePerLiter
                    FuelType.HYBRID -> petrolPricePerLiter
                    FuelType.ELECTRIC, null -> petrolPricePerLiter
                }
                litersPer100Km.multiply(pricePerLiter, decimalMode)
                    .divide(hundred, decimalMode)
            }

            CarCategory.FCEV -> {
                // Fuel cell: placeholder calculation at €9/100km
                BigDecimal.parseString("9.00").divide(hundred, decimalMode)
            }
        }

        // Maintenance cost per kilometer (category-dependent)
        val maintenancePerKm = when (car.category) {
            CarCategory.BEV -> BigDecimal.parseString("0.04")
            CarCategory.ICE -> BigDecimal.parseString("0.06")
            CarCategory.FCEV -> BigDecimal.parseString("0.07")
        }

        // Total cost per km = energy + maintenance
        return energyCostPerKm.add(maintenancePerKm, decimalMode)
            .roundToDigitPositionAfterDecimalPoint(4, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
    }
}
