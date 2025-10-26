package dev.koenv.rentmycar.plugins.di

import dev.koenv.rentmycar.domain.service.*
import org.koin.dsl.module

val serviceModule = module {
    single { AuthService(get(), get()) }
    single { CarAvailabilityService(get()) }
    single { CarPhotoService(get()) }
    single { CarService(get()) }
    single { DrivingSessionService(get()) }
    single { ReservationService(get(), get(), get()) }
    single { SearchService(get()) }
    single { UserService(get()) }
}
