package dev.koenv.rentmycar.app.util

import androidx.compose.runtime.Composable

interface MapsNavigator {
    fun open(lat: Double, lng: Double, label: String? = null)
}

@Composable
expect fun rememberMapsNavigator(): MapsNavigator
