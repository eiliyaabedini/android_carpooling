package com.deftmove.carpooling.interfaces.ride.create.model

import com.deftmove.carpooling.interfaces.ride.model.RideForFeed
import com.deftmove.carpooling.interfaces.ride.model.RideRole
import java.io.Serializable

data class CreateRideLaunchModel(val role: RideRole, val rideForUpdate: RideForFeed?, val updateRepeatingRides: Boolean?) :
    Serializable

data class CreateRideAddWayPointModel(val model: CreateRideModel) : Serializable
data class CreateRideReviewModel(val model: CreateRideModel) : Serializable
