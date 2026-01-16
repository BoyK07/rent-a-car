package dev.koenv.rentmycar.server.routes

import dev.koenv.rentmycar.server.routes.api.v1.auth.AuthRoutes
import dev.koenv.rentmycar.server.routes.api.v1.availability.AvailabilityRoutes
import dev.koenv.rentmycar.server.routes.api.v1.cars.CarAvailabilityRoutes
import dev.koenv.rentmycar.server.routes.api.v1.cars.CarCostRoutes
import dev.koenv.rentmycar.server.routes.api.v1.cars.CarPhotoRoutes
import dev.koenv.rentmycar.server.routes.api.v1.cars.CarRoutes
import dev.koenv.rentmycar.server.routes.api.v1.reservations.ReservationRoutes
import dev.koenv.rentmycar.server.routes.api.v1.users.UserRoutes
import dev.koenv.rentmycar.server.routes.root.RootRoutes
import io.ktor.server.routing.*

/**
 * Base interface for route tree nodes.
 * 
 * Used to organize routes in a hierarchical structure for clean registration.
 */
sealed interface RouteNode

/**
 * Interface for route registrars that can register themselves on a Ktor Route.
 * 
 * Implementing classes should define their routes using Ktor's routing DSL
 * within the register() function.
 * 
 * Example:
 * ```kotlin
 * object MyRoutes : RouteRegistrar {
 *     override fun Route.register() {
 *         get("/my-endpoint") { ... }
 *     }
 * }
 * ```
 */
interface RouteRegistrar : RouteNode {
    /**
     * Registers this route handler's endpoints on the given Route.
     * 
     * @receiver Route The Ktor Route to register endpoints on
     */
    fun Route.register()
}

/**
 * Registers all application routes on the root Route.
 * 
 * This function acts as the central registry for all API endpoints in the application.
 * Routes are organized hierarchically:
 * - Root routes (health checks, status)
 * - API v1 routes (versioned REST API)
 *   - Auth endpoints (login, register)
 *   - User management
 *   - Car management and photos
 *   - Availability and reservations
 *   - Cost calculations
 * 
 * The tree structure ensures all routes are registered in a consistent order
 * and makes it easy to see the complete API structure at a glance.
 * 
 * @param root The root Route to register all endpoints on
 */
fun registerAllRoutes(root: Route) {
    val tree: List<RouteNode> = listOf(
        // =====================================
        // Root routes
        // =====================================
        RootRoutes,

        // =====================================
        // API v1 routes
        // =====================================
        AuthRoutes,
        UserRoutes,
        CarRoutes,
        CarPhotoRoutes,
        CarAvailabilityRoutes,
        AvailabilityRoutes,
        CarCostRoutes,
        ReservationRoutes,
    )

    /**
     * Recursively registers a RouteNode on this Route.
     * 
     * @param node The RouteNode to register
     * @throws IllegalStateException if node type is not recognized
     */
    fun Route.registerNode(node: RouteNode) {
        when (node) {
            is RouteRegistrar -> with(node) { register() }
            else -> error("Unknown node type: ${node.javaClass.canonicalName}")
        }
    }

    // Register all routes in the tree
    tree.forEach { root.registerNode(it) }
}
