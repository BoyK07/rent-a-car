package dev.koenv.rentmycar.server.plugins.di

import dev.koenv.rentmycar.server.storage.repository.*
import org.koin.dsl.module

val repositoryModule = module {
    single { CarAvailabilityRepositoryImpl() }
    single { CarPhotoRepositoryImpl() }
    single { CarRepositoryImpl() }
    single { DrivingSessionRepositoryImpl() }
    single { ReservationRepositoryImpl() }
    single { UserRepositoryImpl() }
}
