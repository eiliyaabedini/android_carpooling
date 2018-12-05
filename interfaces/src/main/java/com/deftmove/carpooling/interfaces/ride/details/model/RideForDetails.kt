package com.deftmove.carpooling.interfaces.ride.details.model

import com.deftmove.carpooling.interfaces.ride.model.Invitation
import com.deftmove.carpooling.interfaces.ride.model.Recommendation
import com.deftmove.carpooling.interfaces.ride.model.RideRole
import com.deftmove.heart.interfaces.map.Location
import com.github.vivchar.rendererrecyclerviewadapter.ViewModel
import java.util.Date

data class RideForDetails(
    val id: String,
    val origin: Location,
    val destination: Location,
    val polyline: String?,
    val recommendationsCount: Int?,
    val role: RideRole,
    val time: Date,

    val invitations: List<Invitation>,
    val recommendations: List<Recommendation>
) : ViewModel {

    override fun equals(other: Any?): Boolean {
        return other is RideForDetails &&
              other.id == this.id &&
              other.origin == this.origin &&
              other.destination == this.destination &&
              other.polyline == this.polyline &&
              other.recommendationsCount == this.recommendationsCount &&
              other.role == this.role &&
              other.time == this.time &&
              other.invitations == this.invitations &&
              other.recommendations == this.recommendations
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
