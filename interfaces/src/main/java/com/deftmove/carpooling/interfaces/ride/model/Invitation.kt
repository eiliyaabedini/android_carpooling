package com.deftmove.carpooling.interfaces.ride.model

import com.deftmove.carpooling.interfaces.authentication.model.UserModel
import com.deftmove.heart.interfaces.common.model.Money
import com.deftmove.heart.interfaces.map.LocationWithTime
import java.util.Date

data class Invitation(
    val id: String,
    val cancelledAt: Date?,
    val confirmedAt: Date?,
    val offerId: String,
    val requestId: String,
    val senderId: String,
    val passengerId: String,
    val driverId: String,
    val sharedRoute: String?,
    val pickup: LocationWithTime?,
    val dropoff: LocationWithTime?,
    val grossPrice: Money?,
    val driver: UserModel,
    val passenger: UserModel
)
