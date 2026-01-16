package dev.koenv.rentmycar.server.plugins.di

import dev.koenv.rentmycar.server.domain.service.*
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
    single { AuthService(get(), get()) }
    single { CarAvailabilityService(get()) }
    single { CarPhotoService(get()) }
    single { CarService(get()) }
    single { ReservationService(get(), get(), get()) }
    single { DrivingSessionService(get(), get()) }
    single { SearchService(get()) }
    single { UserService(get()) }
}
