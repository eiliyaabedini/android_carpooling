package com.deftmove.carpooling.interfaces.ride.repository

import com.deftmove.heart.interfaces.map.Location
import com.deftmove.heart.interfaces.map.Route

interface WaypointsUpdateRepository {

    fun addWaypoints(requestId: Int, locations: List<Location>)

    fun updateRoutes(requestId: Int, routes: List<Route>): List<Route>
}
