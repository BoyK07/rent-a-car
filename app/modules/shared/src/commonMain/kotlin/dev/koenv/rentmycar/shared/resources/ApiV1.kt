package dev.koenv.rentmycar.shared.resources

import io.ktor.resources.*
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * Root API resource for v1 endpoints
 */
@Resource("/api/v1")
@Serializable
class ApiV1 {
    
    // =====================================
    // Authentication Resources
    // =====================================
    
    @Resource("/auth")
    @Serializable
    class Auth(val parent: ApiV1 = ApiV1()) {
        @Resource("/register")
        @Serializable
        class Register(val parent: Auth = Auth())
        
        @Resource("/login")
        @Serializable
        class Login(val parent: Auth = Auth())
    }
    
    // =====================================
    // User Resources
    // =====================================
    
    @Resource("/users")
    @Serializable
    class Users(val parent: ApiV1 = ApiV1()) {
        @Resource("/{id}")
        @Serializable
        class Id(val parent: Users = Users(), val id: String)
    }
    
    // =====================================
    // Car Resources
    // =====================================
    
    @Resource("/cars")
    @Serializable
    class Cars(
        val parent: ApiV1 = ApiV1(),
        // Search/filter parameters
        val page: Int? = null,
        val limit: Int? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val maxDistance: Double? = null,
        val minPrice: String? = null,
        val maxPrice: String? = null,
        val brand: String? = null,
        val category: String? = null,
        val fuelType: String? = null,
        val isActive: Boolean? = null,
        val ownerId: String? = null,
        val maxRate: String? = null
    ) {
        @Resource("/{id}")
        @Serializable
        class Id(val parent: Cars = Cars(), val id: String) {
            @Resource("/tco")
            @Serializable
            class Tco(val parent: Id = Id(id = ""), val annualKm: Int)
            
            @Resource("/cost-per-km")
            @Serializable
            class CostPerKm(val parent: Id = Id(id = ""))
            
            // Car Photos nested under specific car
            @Resource("/photos")
            @Serializable
            class Photos(val parent: Id = Id(id = "")) {
                @Resource("/{photoId}")
                @Serializable
                class PhotoId(val parent: Photos, val photoId: String)
                
                @Resource("/upload")
                @Serializable
                class Upload(val parent: Photos)
            }
            
            // Car Availability nested under specific car
            @Resource("/availability")
            @Serializable
            class Availability(val parent: Id = Id(id = "")) {
                @Resource("/{availabilityId}")
                @Serializable
                class AvailabilityId(val parent: Availability, val availabilityId: String)
            }
        }
    }
    
    // =====================================
    // Availability Resources (Query endpoint)
    // =====================================
    
    @Resource("/availability")
    @Serializable
    class Availability(
        val parent: ApiV1 = ApiV1(),
        val carId: String,
        val start: String,
        val end: String
    )
    
    // =====================================
    // Car Availability Resources (for client API compatibility)
    // Server uses nested Cars.Id.Availability resources
    // =====================================
    
    @Resource("/car-availability")
    @Serializable
    class CarAvailability(
        val parent: ApiV1 = ApiV1(),
        val carId: String? = null
    ) {
        @Resource("/{id}")
        @Serializable
        class Id(val parent: CarAvailability = CarAvailability(), val id: String)
    }
    
    // =====================================
    // Car Photos Resources (for client API compatibility)
    // Server uses nested Cars.Id.Photos resources
    // =====================================
    
    @Resource("/car-photos")
    @Serializable
    class CarPhotos(val parent: ApiV1 = ApiV1()) {
        @Resource("/{id}")
        @Serializable
        class Id(val parent: CarPhotos = CarPhotos(), val id: String)
    }
    
    // =====================================
    // Car Cost Resources
    // =====================================
    
    @Resource("/car-cost")
    @Serializable
    class CarCost(
        val parent: ApiV1 = ApiV1(),
        val carId: String,
        val start: String,
        val end: String
    )
    
    // =====================================
    // Reservation Resources
    // =====================================
    
    @Resource("/reservations")
    @Serializable
    class Reservations(
        val parent: ApiV1 = ApiV1(),
        val renterId: String? = null,
        val carId: String? = null,
        val status: String? = null,
        val start: String? = null,
        val end: String? = null
    ) {
        @Resource("/{id}")
        @Serializable
        class Id(val parent: Reservations = Reservations(), val id: String) {
            @Resource("/cancel")
            @Serializable
            class Cancel(val parent: Id = Id(id = ""))
            
            @Resource("/confirm")
            @Serializable
            class Confirm(val parent: Id = Id(id = ""))
            
            @Resource("/complete")
            @Serializable
            class Complete(val parent: Id = Id(id = ""))
            
            @Resource("/driving-sessions")
            @Serializable
            class DrivingSessions(val parent: Id = Id(id = ""))
        }
        
        @Resource("/quote")
        @Serializable
        class Quote(val parent: Reservations = Reservations())
        
        @Resource("/active")
        @Serializable
        class Active(val parent: Reservations = Reservations())
        
        @Resource("/my-cars")
        @Serializable
        class MyCars(val parent: Reservations = Reservations())
    }
}
