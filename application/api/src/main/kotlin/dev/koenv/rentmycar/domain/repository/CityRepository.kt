package dev.koenv.rentmycar.domain.repository

import dev.koenv.rentmycar.domain.entity.City
import java.util.UUID

interface CityRepository : Repository<City, UUID> {}
