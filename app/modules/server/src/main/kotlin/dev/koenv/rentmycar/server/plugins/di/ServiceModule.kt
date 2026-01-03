package dev.koenv.rentmycar.server.plugins.di

import dev.koenv.rentmycar.server.domain.service.*
import org.koin.dsl.module

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
