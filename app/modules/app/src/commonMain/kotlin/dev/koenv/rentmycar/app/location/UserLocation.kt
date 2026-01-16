package dev.koenv.rentmycar.app.location

import androidx.compose.runtime.Composable

@Composable
expect fun rememberUserLocation(onLocation: (lat: Double, lng: Double) -> Unit)
