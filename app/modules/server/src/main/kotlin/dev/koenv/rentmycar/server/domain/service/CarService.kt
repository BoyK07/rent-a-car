package dev.koenv.rentmycar.server.domain.service

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import dev.koenv.rentmycar.server.storage.repository.CarRepositoryImpl
import dev.koenv.rentmycar.shared.domain.entity.Car
import dev.koenv.rentmycar.shared.domain.enums.CarCategory
import dev.koenv.rentmycar.shared.domain.enums.FuelType
import kotlin.uuid.Uuid

class CarService(private val repo: CarRepositoryImpl) {
    private val decimalMode = DecimalMode(decimalPrecision = 10, roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)

    suspend fun getAll(): List<Car> = repo.findAll()
    suspend fun getById(id: Uuid): Car? = repo.findById(id)
    suspend fun create(car: Car): Car = repo.create(car)
    suspend fun update(id: Uuid, car: Car): Car? = repo.update(id, car)
    suspend fun delete(id: Uuid): Boolean = repo.delete(id)
    suspend fun count(): Long = repo.count()

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

    fun calculateTcoPerYear(car: Car, annualKm: Int = 15000): BigDecimal {
        // Cost per km component (energy + maintenance)
        val variableCostPerKm = calculateCostPerKm(car)

        // Fixed overheads (e.g., insurance, tax, parking), category-based defaults
        val fixedOverhead = when (car.category) {
            CarCategory.ICE -> BigDecimal.parseString("1000.00")
            CarCategory.BEV -> BigDecimal.parseString("800.00")
            CarCategory.FCEV -> BigDecimal.parseString("1200.00")
        }

        val annualKmDecimal = BigDecimal.fromInt(annualKm)
        val distanceComponent = variableCostPerKm.multiply(annualKmDecimal, decimalMode)
        return distanceComponent.add(fixedOverhead, decimalMode)
            .roundToDigitPositionAfterDecimalPoint(2, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
    }

    fun calculateCostPerKm(car: Car): BigDecimal {
        // Energy price assumptions
        val petrolPricePerLiter = BigDecimal.parseString("1.90")
        val dieselPricePerLiter = BigDecimal.parseString("1.70")
        val lpgPricePerLiter = BigDecimal.parseString("1.05")
        val electricityPricePerKwh = BigDecimal.parseString("0.30")

        // Efficiency assumptions
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

        val kwhPer100Km = when (car.category) {
            CarCategory.BEV -> BigDecimal.parseString("18.0") // kWh/100km
            CarCategory.ICE -> BigDecimal.ZERO
            CarCategory.FCEV -> BigDecimal.ONE // placeholder kg H2 equivalent
        }

        val hundred = BigDecimal.parseString("100.0")

        // Energy cost per km component
        val energyCostPerKm: BigDecimal = when (car.category) {
            CarCategory.BEV ->
                // (kWh/100km * price) / 100
                kwhPer100Km.multiply(electricityPricePerKwh, decimalMode).divide(hundred, decimalMode)

            CarCategory.ICE -> {
                val pricePerLiter = when (car.fuelType) {
                    FuelType.PETROL -> petrolPricePerLiter
                    FuelType.DIESEL -> dieselPricePerLiter
                    FuelType.LPG -> lpgPricePerLiter
                    FuelType.HYBRID -> petrolPricePerLiter
                    FuelType.ELECTRIC, null -> petrolPricePerLiter
                }
                litersPer100Km.multiply(pricePerLiter, decimalMode).divide(hundred, decimalMode)
            }

            CarCategory.FCEV ->
                // Placeholder: treat as 9 EUR / 100km
                BigDecimal.parseString("9.00").divide(hundred, decimalMode)
        }

        // Maintenance/tires/etc per km
        val maintenancePerKm = when (car.category) {
            CarCategory.BEV -> BigDecimal.parseString("0.04")
            CarCategory.ICE -> BigDecimal.parseString("0.06")
            CarCategory.FCEV -> BigDecimal.parseString("0.07")
        }

        return energyCostPerKm.add(maintenancePerKm, decimalMode)
            .roundToDigitPositionAfterDecimalPoint(4, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
    }
}
