package com.deftmove.ride.repository

import com.deftmove.carpooling.interfaces.ride.repository.WaypointsUpdateRepository
import com.deftmove.heart.common.repository.InMemoryPagingRepository
import com.deftmove.heart.interfaces.map.Location
import com.deftmove.heart.interfaces.map.Route

class WaypointsUpdateRepositoryImp : InMemoryPagingRepository<Int, Location>(), WaypointsUpdateRepository {

    override fun addWaypoints(requestId: Int, locations: List<Location>) {
        addItemsToPage(locations, requestId)
    }

    override fun updateRoutes(requestId: Int, routes: List<Route>): List<Route> {
        val newRoutes: MutableList<Route> = mutableListOf()

        routes.forEach { route ->
            val newStops: MutableList<Location> = mutableListOf()

            route.stopOrder?.forEach { stopOrder ->
                val oldStop = route.stops[stopOrder.toInt()]
                newStops.add(
                      Location(
                            duration = oldStop.duration,
                            name = getAllItemsForPage(requestId)?.get(stopOrder.toInt())?.name,
                            address = oldStop.address,
                            coordinate = oldStop.coordinate
                      )
                )
            }

            val newRoute = Route(
                  distance = route.distance,
                  duration = route.duration,
                  polyline = route.polyline,
                  stopOrder = route.stopOrder,
                  stops = newStops
            )

            newRoutes.add(newRoute)
        }

        return newRoutes
    }
}
