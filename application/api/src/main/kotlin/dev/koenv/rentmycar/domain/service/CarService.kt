package dev.koenv.rentmycar.domain.service

import dev.koenv.rentmycar.domain.entity.Car
import dev.koenv.rentmycar.domain.enums.CarCategory
import dev.koenv.rentmycar.domain.enums.FuelType
import dev.koenv.rentmycar.domain.repository.CarRepository
import kotlinx.datetime.LocalDateTime
import java.math.BigDecimal
import java.util.UUID

class CarService(private val repo: CarRepository) {
    suspend fun getAll(): List<Car> = repo.findAll()
    suspend fun getById(id: UUID): Car? = repo.findById(id)
    suspend fun create(car: Car): Car = repo.create(car)
    suspend fun update(id: UUID, car: Car): Car? = repo.update(id, car)
    suspend fun delete(id: UUID): Boolean = repo.delete(id)
    suspend fun count(): Long = repo.count()

    suspend fun listFiltered(
        ownerId: UUID? = null,
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

    suspend fun findAvailableCarsInRange(
        start: LocalDateTime,
        end: LocalDateTime,
        maxRate: BigDecimal? = null
    ): List<Car> {
        TODO("Filter by availability and rate using repositories")
    }

    suspend fun findCarsNearLocation(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): List<Car> {
        TODO("Return cars within radiusKm of given coordinates")
    }

    fun calculateTcoPerYear(car: Car, annualKm: Int = 15000): BigDecimal {
        // Cost per km component (energy + maintenance)
        val variableCostPerKm = calculateCostPerKm(car)

        // Fixed overheads (e.g., insurance, tax, parking), category-based defaults
        val fixedOverhead = when (car.category) {
            dev.koenv.rentmycar.domain.enums.CarCategory.ICE -> BigDecimal("1000.00")
            dev.koenv.rentmycar.domain.enums.CarCategory.BEV -> BigDecimal("800.00")
            dev.koenv.rentmycar.domain.enums.CarCategory.FCEV -> BigDecimal("1200.00")
        }

        val distanceComponent = variableCostPerKm.multiply(BigDecimal(annualKm))
        return distanceComponent.add(fixedOverhead)
    }

    fun calculateCostPerKm(car: Car): BigDecimal {
        // Energy price assumptions
        val petrolPricePerLiter = BigDecimal("1.90")
        val dieselPricePerLiter = BigDecimal("1.70")
        val lpgPricePerLiter = BigDecimal("1.05")
        val electricityPricePerKwh = BigDecimal("0.30")

        // Efficiency assumptions
        val litersPer100Km = when (car.fuelType) {
            dev.koenv.rentmycar.domain.enums.FuelType.PETROL -> BigDecimal("7.0")
            dev.koenv.rentmycar.domain.enums.FuelType.DIESEL -> BigDecimal("6.0")
            dev.koenv.rentmycar.domain.enums.FuelType.LPG -> BigDecimal("8.0")
            dev.koenv.rentmycar.domain.enums.FuelType.HYBRIDE -> BigDecimal("5.0")
            dev.koenv.rentmycar.domain.enums.FuelType.ELECTRIC -> BigDecimal.ZERO
            null -> when (car.category) {
                dev.koenv.rentmycar.domain.enums.CarCategory.BEV -> BigDecimal.ZERO
                else -> BigDecimal("7.0")
            }
        }

        val kwhPer100Km = when (car.category) {
            dev.koenv.rentmycar.domain.enums.CarCategory.BEV -> BigDecimal("18.0") // kWh/100km
            dev.koenv.rentmycar.domain.enums.CarCategory.ICE -> BigDecimal.ZERO
            dev.koenv.rentmycar.domain.enums.CarCategory.FCEV -> BigDecimal("1.0") // placeholder kg H2 equivalent below
        }

        // Energy cost per km component
        val energyCostPerKm: BigDecimal = when (car.category) {
            dev.koenv.rentmycar.domain.enums.CarCategory.BEV ->
                // (kWh/100km * price) / 100
                kwhPer100Km.multiply(electricityPricePerKwh).divide(BigDecimal(100))
            dev.koenv.rentmycar.domain.enums.CarCategory.ICE -> {
                val pricePerLiter = when (car.fuelType) {
                    dev.koenv.rentmycar.domain.enums.FuelType.PETROL -> petrolPricePerLiter
                    dev.koenv.rentmycar.domain.enums.FuelType.DIESEL -> dieselPricePerLiter
                    dev.koenv.rentmycar.domain.enums.FuelType.LPG -> lpgPricePerLiter
                    dev.koenv.rentmycar.domain.enums.FuelType.HYBRIDE -> petrolPricePerLiter
                    dev.koenv.rentmycar.domain.enums.FuelType.ELECTRIC, null -> petrolPricePerLiter
                }
                litersPer100Km.multiply(pricePerLiter).divide(BigDecimal(100))
            }
            dev.koenv.rentmycar.domain.enums.CarCategory.FCEV ->
                // Placeholder: treat as 9 EUR / 100km
                BigDecimal("9.00").divide(BigDecimal(100))
        }

        // Maintenance/tires/etc per km
        val maintenancePerKm = when (car.category) {
            dev.koenv.rentmycar.domain.enums.CarCategory.BEV -> BigDecimal("0.04")
            dev.koenv.rentmycar.domain.enums.CarCategory.ICE -> BigDecimal("0.06")
            dev.koenv.rentmycar.domain.enums.CarCategory.FCEV -> BigDecimal("0.07")
        }

        return energyCostPerKm.add(maintenancePerKm)
    }
}
