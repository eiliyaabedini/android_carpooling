package com.deftmove.carpooling.interfaces.ride.model

import com.deftmove.carpooling.interfaces.authentication.model.UserModel
import com.deftmove.carpooling.interfaces.ride.create.model.RepeatingRideModel
import com.deftmove.heart.interfaces.common.model.Money
import com.deftmove.heart.interfaces.map.Location
import com.deftmove.heart.interfaces.map.Route
import com.github.vivchar.rendererrecyclerviewadapter.ViewModel
import java.io.Serializable
import java.util.Date

data class RideForFeed(
    val id: String,
    val origin: Location,
    val destination: Location,
    val route: Route?,
    val role: RideRole,
    val time: Date,
    val recommendationsCount: Int,
    val repeat: RepeatingRideModel,

      //From here are the values that are not part of the RideForFeed but we moved them here
    val offeredCount: Int,
    val requestedCount: Int,
    val confirmedCount: Int,
    val cancelledCount: Int?,
    val declinedCount: Int?,
    val sumConfirmedPrice: Money,
    val driver: UserModel?
) : ViewModel, Serializable {

    fun getOffersAndRequestedSum(): Int = offeredCount.plus(requestedCount)
}
