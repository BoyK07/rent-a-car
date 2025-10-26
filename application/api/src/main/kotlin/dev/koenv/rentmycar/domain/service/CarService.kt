package dev.koenv.rentmycar.domain.service

import dev.koenv.rentmycar.domain.entity.Car
import dev.koenv.rentmycar.domain.enums.CarCategory
import dev.koenv.rentmycar.domain.enums.FuelType
import dev.koenv.rentmycar.domain.repository.CarRepository
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

    fun calculateTcoPerYear(car: Car, annualKm: Int = 15000): BigDecimal {
        // Cost per km component (energy + maintenance)
        val variableCostPerKm = calculateCostPerKm(car)

        // Fixed overheads (e.g., insurance, tax, parking), category-based defaults
        val fixedOverhead = when (car.category) {
            CarCategory.ICE -> BigDecimal("1000.00")
            CarCategory.BEV -> BigDecimal("800.00")
            CarCategory.FCEV -> BigDecimal("1200.00")
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
            FuelType.PETROL -> BigDecimal("7.0")
            FuelType.DIESEL -> BigDecimal("6.0")
            FuelType.LPG -> BigDecimal("8.0")
            FuelType.HYBRIDE -> BigDecimal("5.0")
            FuelType.ELECTRIC -> BigDecimal.ZERO
            null -> when (car.category) {
                CarCategory.BEV -> BigDecimal.ZERO
                else -> BigDecimal("7.0")
            }
        }

        val kwhPer100Km = when (car.category) {
            CarCategory.BEV -> BigDecimal("18.0") // kWh/100km
            CarCategory.ICE -> BigDecimal.ZERO
            CarCategory.FCEV -> BigDecimal("1.0") // placeholder kg H2 equivalent below
        }

        // Energy cost per km component
        val energyCostPerKm: BigDecimal = when (car.category) {
            CarCategory.BEV ->
                // (kWh/100km * price) / 100
                kwhPer100Km.multiply(electricityPricePerKwh).divide(BigDecimal(100))
            CarCategory.ICE -> {
                val pricePerLiter = when (car.fuelType) {
                    FuelType.PETROL -> petrolPricePerLiter
                    FuelType.DIESEL -> dieselPricePerLiter
                    FuelType.LPG -> lpgPricePerLiter
                    FuelType.HYBRIDE -> petrolPricePerLiter
                    FuelType.ELECTRIC, null -> petrolPricePerLiter
                }
                litersPer100Km.multiply(pricePerLiter).divide(BigDecimal(100))
            }
            CarCategory.FCEV ->
                // Placeholder: treat as 9 EUR / 100km
                BigDecimal("9.00").divide(BigDecimal(100))
        }

        // Maintenance/tires/etc per km
        val maintenancePerKm = when (car.category) {
            CarCategory.BEV -> BigDecimal("0.04")
            CarCategory.ICE -> BigDecimal("0.06")
            CarCategory.FCEV -> BigDecimal("0.07")
        }

        return energyCostPerKm.add(maintenancePerKm)
    }
}
