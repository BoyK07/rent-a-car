package dev.koenv.rentmycar.app.screens.map

import cafe.adriel.voyager.core.screen.Screen
import dev.koenv.rentmycar.shared.dto.car.CarDto

expect class MapScreen(
    cars: List<CarDto>,
    userLat: Double,
    userLng: Double,
    showNearby: Boolean,
    maxDistanceKm: Int
) : Screen
