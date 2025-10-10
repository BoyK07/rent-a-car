package dev.koenv.rentmycar.domain.repositories

import dev.koenv.rentmycar.domain.model.User
import java.util.UUID

interface UserRepository : Repository<User, UUID> {}
