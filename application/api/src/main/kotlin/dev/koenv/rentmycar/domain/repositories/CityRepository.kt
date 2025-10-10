package dev.koenv.rentmycar.domain.repositories

import dev.koenv.rentmycar.domain.model.City
import java.util.UUID

interface CityRepository : Repository<City, UUID> {}
