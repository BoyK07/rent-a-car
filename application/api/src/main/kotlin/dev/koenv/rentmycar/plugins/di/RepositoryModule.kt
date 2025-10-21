package dev.koenv.rentmycar.plugins.di

import dev.koenv.rentmycar.domain.repository.*
import dev.koenv.rentmycar.storage.repository.*
import org.koin.dsl.module

val repositoryModule = module {
    single<UserRepository> { UserRepositoryImpl() }
    single<CarRepository> { CarRepositoryImpl() }
    single<CarPhotoRepository> { CarPhotoRepositoryImpl() }
    single<CarAvailabilityRepository> { CarAvailabilityRepositoryImpl() }
    single<ReservationRepository> { ReservationRepositoryImpl() }
    single<DrivingSessionRepository> { DrivingSessionRepositoryImpl() }
}
