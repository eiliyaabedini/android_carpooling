package com.deftmove.ride.details.model

import com.deftmove.carpooling.interfaces.ride.model.Invitation
import com.deftmove.carpooling.interfaces.ride.model.Recommendation
import com.github.vivchar.rendererrecyclerviewadapter.ViewModel

sealed class RideDetailsViewModel(val id: String) : ViewModel {

    override fun equals(other: Any?): Boolean {
        return other is RideDetailsViewModel && other.id == this.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    //Roles(Passenger/Driver) here means what is the role of current user in this invitation

    object RideDetailsPassengerEmptyViewModel : RideDetailsViewModel("")

    data class RideDetailsPassengerConfirmedViewModel(val invitation: Invitation) : RideDetailsViewModel(invitation.id)

    data class RideDetailsPassengerOfferedViewModel(val invitation: Invitation) : RideDetailsViewModel(invitation.id)

    data class RideDetailsPassengerRequestedViewModel(val invitation: Invitation) : RideDetailsViewModel(invitation.id)

    data class RideDetailsPassengerRecommendationViewModel(val recommendation: Recommendation) :
        RideDetailsViewModel(recommendation.id)

    data class RideDetailsDriverEmptyViewModel(val confirmedPassengerCount: Int) : RideDetailsViewModel("")

    data class RideDetailsDriverConfirmedViewModel(val invitation: Invitation) : RideDetailsViewModel(invitation.id)

    data class RideDetailsDriverOfferedViewModel(val invitation: Invitation) : RideDetailsViewModel(invitation.id)

    data class RideDetailsDriverRequestedViewModel(val invitation: Invitation) : RideDetailsViewModel(invitation.id)

    data class RideDetailsDriverRecommendationViewModel(val recommendation: Recommendation) :
        RideDetailsViewModel(recommendation.id)

    data class RideDetailsCancelModel(val invitation: Invitation) : RideDetailsViewModel(invitation.id)

}
