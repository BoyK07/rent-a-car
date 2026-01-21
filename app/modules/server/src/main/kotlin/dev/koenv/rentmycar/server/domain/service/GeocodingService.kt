package dev.koenv.rentmycar.server.domain.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class GeocodingResult(
    val latitude: Double,
    val longitude: Double,
    val formattedAddress: String
)

class GeocodingService(
    private val httpClient: HttpClient,
    private val apiKey: String
) {
    suspend fun geocode(address: String): GeocodingResult {
        if (apiKey.isBlank()) {
            throw IllegalStateException("Google Geocoding API key is not configured")
        }

        val response: GoogleGeocodeResponse = httpClient.get("https://maps.googleapis.com/maps/api/geocode/json") {
            parameter("address", address)
            parameter("key", apiKey)
        }.body()

        if (response.status != "OK" || response.results.isEmpty()) {
            val details = response.errorMessage ?: response.status
            throw IllegalArgumentException("Unable to geocode address: $details")
        }

        val result = response.results.first()
        return GeocodingResult(
            latitude = result.geometry.location.lat,
            longitude = result.geometry.location.lng,
            formattedAddress = result.formattedAddress
        )
    }
}

@Serializable
private data class GoogleGeocodeResponse(
    val status: String,
    val results: List<GoogleGeocodeResult> = emptyList(),
    @SerialName("error_message")
    val errorMessage: String? = null
)

@Serializable
private data class GoogleGeocodeResult(
    @SerialName("formatted_address")
    val formattedAddress: String,
    val geometry: GoogleGeocodeGeometry
)

@Serializable
private data class GoogleGeocodeGeometry(
    val location: GoogleGeocodeLocation
)

@Serializable
private data class GoogleGeocodeLocation(
    val lat: Double,
    val lng: Double
)
