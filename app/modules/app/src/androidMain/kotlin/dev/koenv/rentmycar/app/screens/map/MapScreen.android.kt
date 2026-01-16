package dev.koenv.rentmycar.app.screens.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import dev.koenv.rentmycar.app.ui.AppTheme
import dev.koenv.rentmycar.app.ui.components.Icon
import dev.koenv.rentmycar.app.ui.components.IconButton
import dev.koenv.rentmycar.app.ui.components.IconButtonVariant
import dev.koenv.rentmycar.app.ui.components.Scaffold
import dev.koenv.rentmycar.app.ui.components.Text
import dev.koenv.rentmycar.app.ui.components.topbar.TopBar
import dev.koenv.rentmycar.shared.dto.car.CarDto
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

actual class MapScreen actual constructor(
    private val cars: List<CarDto>,
    private val userLat: Double,
    private val userLng: Double,
    private val showNearby: Boolean,
    private val maxDistanceKm: Int
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val center = remember { LatLng(userLat, userLng) }
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(center, 12f)
        }

        Scaffold(
            topBar = {
                TopBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(
                            onClick = { navigator.pop() },
                            variant = IconButtonVariant.Ghost
                        ) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text(
                            text = "Nearby Cars",
                            style = AppTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = rememberMarkerState(position = center),
                        title = "You are here"
                    )

                    if (showNearby) {
                        Circle(
                            center = center,
                            radius = maxDistanceKm * 1000.0,
                            fillColor = Color(0x224A90E2),
                            strokeColor = Color(0xFF4A90E2),
                            strokeWidth = 2f
                        )
                    }

                    cars.forEach { car ->
                        val carPosition = LatLng(car.locationLat, car.locationLng)
                        Marker(
                            state = MarkerState(carPosition),
                            title = "${car.brand} ${car.model}",
                            snippet = formatAddress(car)
                        )
                    }
                }
            }
        }
    }
}

private fun formatAddress(car: CarDto): String? {
    car.formattedAddress?.let { return it }

    val parts = listOfNotNull(
        car.addressLine1,
        car.addressLine2,
        car.postalCode,
        car.city,
        car.country
    ).filter { it.isNotBlank() }

    return if (parts.isEmpty()) null else parts.joinToString(", ")
}
