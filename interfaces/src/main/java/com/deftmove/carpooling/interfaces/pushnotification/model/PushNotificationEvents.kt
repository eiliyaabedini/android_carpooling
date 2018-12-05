package com.deftmove.carpooling.interfaces.pushnotification.model

import com.deftmove.heart.common.event.DataEvent

object PushNotificationEvents {
    object RideCreated : DataEvent
    object RideUpdated : DataEvent
    data class RideCancelled(val rideId: String) : DataEvent

    data class InvitationCreated(val invitationId: String) : DataEvent
    data class InvitationConfirmed(val invitationId: String) : DataEvent
    data class InvitationCancelled(val invitationId: String) : DataEvent
    data class RideNotificationReceived(val rideId: String) : DataEvent

}
