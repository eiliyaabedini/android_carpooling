package com.deftmove.carpooling.interfaces.ride.service

import com.deftmove.carpooling.interfaces.ride.create.model.RepeatingRideModel
import com.deftmove.carpooling.interfaces.ride.model.RecommendationOnTheFly
import com.deftmove.carpooling.interfaces.ride.model.RideForFeed
import com.deftmove.heart.interfaces.ResponseResult
import com.deftmove.heart.interfaces.map.Location
import com.deftmove.heart.interfaces.map.Route
import io.reactivex.Single
import java.util.Date

interface RideService {

    fun findRoutes(
        destination: Location,
        origin: Location,
        stops: List<Location>,
        startTime: Date
    ): Single<ResponseResult<List<Route>>>

    fun createRideAsPassenger(
        origin: Location,
        destination: Location,
        startTime: Date,
        repeat: RepeatingRideModel
    ): Single<ResponseResult<RideForFeed>>

    fun updateRideAsPassenger(
        id: String,
        updateRepeatingRides: Boolean?,
        origin: Location,
        destination: Location,
        startTime: Date,
        repeat: RepeatingRideModel
    ): Single<ResponseResult<RideForFeed>>

    fun createRideAsDriver(
        origin: Location,
        destination: Location,
        startTime: Date,
        route: Route?,
        repeat: RepeatingRideModel
    ): Single<ResponseResult<RideForFeed>>

    fun updateRideAsDriver(
        id: String,
        updateRepeatingRides: Boolean?,
        origin: Location,
        destination: Location,
        startTime: Date,
        route: Route?,
        repeat: RepeatingRideModel
    ): Single<ResponseResult<RideForFeed>>

    fun cancelRide(
        rideId: String,
        cancelRepeatingRides: Boolean
    ): Single<ResponseResult<String>>

    fun getRecommendationsOnTheFlyForDriver(
        destination: Location,
        origin: Location,
        polyline: String?,
        startTime: Date
    ): Single<ResponseResult<List<RecommendationOnTheFly>>>
}
