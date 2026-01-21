package dev.koenv.rentmycar.server.plugins.di

import dev.koenv.rentmycar.server.storage.repository.*
import org.koin.dsl.module

/**
 * Koin module for data access layer dependencies.
 * 
 * Registers all repository implementations as singletons:
 * - CarAvailabilityRepositoryImpl
 * - CarPhotoRepositoryImpl
 * - CarRepositoryImpl
 * - DrivingSessionRepositoryImpl
 * - ReservationRepositoryImpl
 * - UserRepositoryImpl
 * 
 * All repositories use Exposed ORM for database access.
 */
val repositoryModule = module {
    single { CarAvailabilityRepositoryImpl() }
    single { CarPhotoRepositoryImpl() }
    single { CarRepositoryImpl() }
    single { DrivingSessionRepositoryImpl() }
    single { ReservationRepositoryImpl() }
    single { UserRepositoryImpl() }
}
