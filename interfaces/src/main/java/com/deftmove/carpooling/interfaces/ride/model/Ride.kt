package com.deftmove.carpooling.interfaces.ride.model

import com.deftmove.heart.interfaces.common.model.Money
import com.deftmove.heart.interfaces.map.Location
import com.github.vivchar.rendererrecyclerviewadapter.ViewModel
import java.util.Date

//    invitations: [Invitation]?
//    recommendations(limit: Intoffset: Int): [Recommendation]?
data class Ride(
    val id: String,
    val origin: Location,
    val destination: Location,
    val distance: Double?,
    val duration: Double?,
    val polyline: String?,
    val role: RideRole,
    val status: String?,
    val time: Date,
    val recommendationsCount: Int?,

      //From here are the values that are not part of the Ride but we moved them here
    val offersOrRequestedCount: Int?,
    val confirmedsCount: Int?,
    val cancelledsCount: Int?,
    val declinedsCount: Int?,
    val sumConfirmedPrice: Money
) : ViewModel {

    override fun equals(other: Any?): Boolean {
        return other is Ride && other.id == this.id
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
