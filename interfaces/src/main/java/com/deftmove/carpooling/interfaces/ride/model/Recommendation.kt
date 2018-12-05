package com.deftmove.carpooling.interfaces.ride.model

import com.deftmove.carpooling.interfaces.authentication.model.UserModel
import com.deftmove.heart.interfaces.common.model.Money
import com.deftmove.heart.interfaces.map.Location
import com.deftmove.heart.interfaces.map.LocationWithTime
import java.util.Date

data class Recommendation(
    val id: String,
    val origin: Location,
    val destination: Location,
    val pickup: LocationWithTime?,
    val dropoff: LocationWithTime?,
    val grossPrice: Money?,
    val role: RideRole,
    val sharedRoute: String?,
    val time: Date,
    val user: UserModel
)

