package dev.koenv.rentmycar.app.util

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberMapsNavigator(): MapsNavigator {
    val context = LocalContext.current
    return object : MapsNavigator {
        override fun open(lat: Double, lng: Double, label: String?) {
            val encodedLabel = label?.let { Uri.encode(it) }
            val geoUri = if (encodedLabel != null) {
                Uri.parse("geo:$lat,$lng?q=$lat,$lng($encodedLabel)")
            } else {
                Uri.parse("geo:$lat,$lng")
            }

            val intent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val canHandle = intent.resolveActivity(context.packageManager) != null
            if (canHandle) {
                context.startActivity(intent)
            } else {
                val fallback = Uri.parse("https://maps.google.com/?q=$lat,$lng")
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, fallback).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }
}
