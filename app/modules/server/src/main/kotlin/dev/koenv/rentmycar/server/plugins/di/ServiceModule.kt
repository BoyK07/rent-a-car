package dev.koenv.rentmycar.server.plugins.di

import dev.koenv.rentmycar.server.domain.service.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

/**
 * Koin module for business logic layer dependencies.
 * 
 * Registers all service implementations as singletons with their dependencies:
 * - AuthService (UserService, AuthService dependencies)
 * - CarAvailabilityService
 * - CarPhotoService
 * - CarService
 * - ReservationService (CarService, UserService, CarAvailabilityService)
 * - DrivingSessionService (ReservationService, UserService)
 * - SearchService (CarService)
 * - UserService
 * 
 * Dependencies are automatically resolved by Koin using get().
 */
val serviceModule = module {
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
    single {
        val config = get<io.ktor.server.config.ApplicationConfig>()
        val apiKey = config.propertyOrNull("geocoding.google.apiKey")?.getString()?.trim().orEmpty()
        GeocodingService(get(), apiKey)
    }
    single { AuthService(get(), get()) }
    single { CarAvailabilityService(get()) }
    single { CarPhotoService(get()) }
    single { CarService(get(), get()) }
    single { ReservationService(get(), get(), get()) }
    single { DrivingSessionService(get(), get()) }
    single { SearchService(get()) }
    single { UserService(get()) }
}
