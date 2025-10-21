package dev.koenv.rentmycar.plugins.di

import dev.koenv.rentmycar.domain.repository.*
import dev.koenv.rentmycar.storage.repository.*
import org.koin.dsl.module

val repositoryModule = module {
    single<CarAvailabilityRepository> { CarAvailabilityRepositoryImpl() }
    single<CarPhotoRepository> { CarPhotoRepositoryImpl() }
    single<CarRepository> { CarRepositoryImpl() }
    single<DrivingSessionRepository> { DrivingSessionRepositoryImpl() }
    single<ReservationRepository> { ReservationRepositoryImpl() }
    single<UserRepository> { UserRepositoryImpl() }
}
