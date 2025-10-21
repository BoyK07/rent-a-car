package dev.koenv.rentmycar.domain.repository

import dev.koenv.rentmycar.domain.entity.Car
import java.util.UUID

interface CarRepository : Repository<Car, UUID>