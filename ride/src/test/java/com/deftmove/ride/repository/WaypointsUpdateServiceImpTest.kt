package com.deftmove.ride.repository

import com.deftmove.heart.interfaces.map.Coordinate
import com.deftmove.heart.interfaces.map.Location
import com.deftmove.heart.interfaces.map.Route
import com.deftmove.heart.testhelper.TestDebugTree
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class WaypointsUpdateServiceImpTest {

    private val waypointsUpdateRepository: WaypointsUpdateRepositoryImp = WaypointsUpdateRepositoryImp()
    private val uniqueId = 15

    @Before
    fun setUp() {
        Timber.plant(TestDebugTree())
    }

    @Test
    fun `When adding items in correct orders and without names then update names and keep the order`() {
        waypointsUpdateRepository.addWaypoints(uniqueId, prepareLocations())

        val oldRoutes = listOf(
              prepareRoute(
                    listOf(
                          prepareLocation(null, "A", 10.0),
                          prepareLocation(null, "B", 20.0),
                          prepareLocation(null, "C", 30.0)
                    )
                    , 0, 1, 2
              )
        )

        val expectedRoutes = listOf(
              prepareRoute(
                    listOf(
                          prepareLocation("A", "A", 10.0),
                          prepareLocation("B", "B", 20.0),
                          prepareLocation("C", "C", 30.0)
                    )
                    , 0, 1, 2
              )
        )

        Assert.assertThat(
              waypointsUpdateRepository.updateRoutes(uniqueId, oldRoutes),
              Matchers.equalTo(expectedRoutes)
        )
    }

    @Test
    fun `When adding items in wrong orders and without names then update names and correct the order`() {
        waypointsUpdateRepository.addWaypoints(uniqueId, prepareSevenLocations())

        val oldRoutes = listOf(
              prepareRoute(
                    listOf(
                          prepareLocation(null, "A", 10.0),
                          prepareLocation(null, "B", 20.0),
                          prepareLocation(null, "C", 30.0),
                          prepareLocation(null, "D", 40.0),
                          prepareLocation(null, "E", 50.0),
                          prepareLocation(null, "F", 60.0),
                          prepareLocation(null, "G", 70.0)
                    )
                    , 1, 0, 2, 4, 5, 3, 6
              )
        )

        val expectedRoutes = listOf(
              prepareRoute(
                    listOf(
                          prepareLocation("B", "B", 20.0),
                          prepareLocation("A", "A", 10.0),
                          prepareLocation("C", "C", 30.0),
                          prepareLocation("E", "E", 50.0),
                          prepareLocation("F", "F", 60.0),
                          prepareLocation("D", "D", 40.0),
                          prepareLocation("G", "G", 70.0)
                    )
                    , 1, 0, 2, 4, 5, 3, 6
              )
        )

        Assert.assertThat(
              waypointsUpdateRepository.updateRoutes(uniqueId, oldRoutes),
              Matchers.equalTo(expectedRoutes)
        )
    }

    private fun prepareLocations(): List<Location> {
        return listOf(
              prepareLocation("A", "A", 10.0),
              prepareLocation("B", "B", 20.0),
              prepareLocation("C", "C", 30.0)
        )
    }

    private fun prepareSevenLocations(): List<Location> {
        return listOf(
              prepareLocation("A", "A", 10.0),
              prepareLocation("B", "B", 20.0),
              prepareLocation("C", "C", 30.0),
              prepareLocation("D", "D", 40.0),
              prepareLocation("E", "E", 50.0),
              prepareLocation("F", "F", 60.0),
              prepareLocation("G", "G", 70.0)
        )
    }

    private fun prepareRoute(stops: List<Location>, vararg stopOrders: Int): Route {
        return Route(
              distance = 123.0,
              duration = 500.0,
              polyline = "polyline here",
              stopOrder = stopOrders.asList(),
              stops = stops
        )
    }

    private fun prepareLocation(name: String?, address: String, location: Double): Location {
        return Location(
              duration = location,
              name = name,
              address = "Location$address address",
              coordinate = Coordinate(location, location)
        )
    }
}
