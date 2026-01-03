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

sealed interface RouteNode

interface RouteRegistrar : RouteNode {
    fun Route.register()
}

data class RouteGroup(
    val prefix: String,
    val children: List<RouteNode>
) : RouteNode

fun registerAllRoutes(root: Route) {
    val tree: List<RouteNode> = listOf(
        RouteGroup(
            "/", listOf(
                RootRoutes,
            )
        ),
        RouteGroup(
            "/api", listOf(
                RouteGroup(
                    "/v1", listOf(
                        AuthRoutes,
                        UserRoutes,
                        CarRoutes,
                        CarPhotoRoutes,
                        CarAvailabilityRoutes,
                        AvailabilityRoutes,
                        CarCostRoutes,
                        ReservationRoutes,
                    )
                )
            )
        )
    )

    fun Route.registerNode(node: RouteNode) {
        when (node) {
            is RouteRegistrar -> with(node) { register() }
            is RouteGroup -> route(node.prefix) {
                node.children.forEach { child -> registerNode(child) }
            }
        }
    }

    tree.forEach { root.registerNode(it) }
}
