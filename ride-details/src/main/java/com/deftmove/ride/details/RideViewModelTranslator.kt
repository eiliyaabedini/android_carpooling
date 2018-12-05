package com.deftmove.ride.details

import com.deftmove.carpooling.interfaces.ride.details.model.RideForDetails
import com.deftmove.carpooling.interfaces.ride.model.Invitation
import com.deftmove.carpooling.interfaces.ride.model.RideRole
import com.deftmove.ride.details.model.RideDetailsViewModel

object RideViewModelTranslator {

    fun translateModels(
        rideForDetails: RideForDetails
    ): List<RideDetailsViewModel> {
        return when (rideForDetails.role) {
            RideRole.DRIVER -> translateDriverModels(rideForDetails)
            RideRole.PASSENGER -> translatePassengerModels(rideForDetails)
        }
    }

    fun translateInvitation(role: RideRole, invitation: Invitation): RideDetailsViewModel {
        return when (role) {
            RideRole.DRIVER -> translateDriverInvitation(invitation)
            RideRole.PASSENGER -> translatePassengerInvitation(invitation)
        }
    }

    fun replaceRecommendationWithInvitation(
        list: MutableList<RideDetailsViewModel>,
        role: RideRole,
        offerId: String,
        requestId: String,
        invitation: Invitation
    ) {
        val recommendationId = when (role) {
            RideRole.DRIVER -> requestId
            RideRole.PASSENGER -> offerId
        }

        val mappedList = list.map { rideDetailsViewModel ->
            if (rideDetailsViewModel.id == recommendationId) {
                translateInvitation(role, invitation)
            } else {
                rideDetailsViewModel
            }
        }

        list.clear()
        list.addAll(mappedList)
    }

    fun replaceInvitationWithInvitation(
        list: MutableList<RideDetailsViewModel>,
        role: RideRole,
        invitationId: String,
        invitation: Invitation
    ) {
        val mappedList = list.map { rideDetailsViewModel ->
            if (rideDetailsViewModel.id == invitationId) {
                translateInvitation(role, invitation)
            } else {
                rideDetailsViewModel
            }
        }

        list.clear()
        list.addAll(mappedList)
    }

    private fun translateDriverInvitation(invitation: Invitation): RideDetailsViewModel {
        return if (invitation.cancelledAt == null && invitation.confirmedAt == null) {
            when (invitation.senderId) {
                invitation.driverId -> {
                    RideDetailsViewModel.RideDetailsDriverOfferedViewModel(
                          invitation
                    )
                }

                invitation.passengerId -> {
                    RideDetailsViewModel.RideDetailsDriverRequestedViewModel(
                          invitation
                    )
                }

                else -> throw IllegalStateException("invitation id:${invitation.id} has illegal state")
            }
        } else if (invitation.cancelledAt == null && invitation.confirmedAt != null) {
            RideDetailsViewModel.RideDetailsDriverConfirmedViewModel(
                  invitation
            )
        } else if (invitation.cancelledAt != null) {
            RideDetailsViewModel.RideDetailsCancelModel(invitation)
        } else {
            throw IllegalStateException("invitation id:${invitation.id} has illegal state which we didn't expected")
        }
    }

    private fun translatePassengerInvitation(invitation: Invitation): RideDetailsViewModel {
        return if (invitation.cancelledAt == null && invitation.confirmedAt == null) {
            when (invitation.senderId) {
                invitation.driverId -> {
                    RideDetailsViewModel.RideDetailsPassengerOfferedViewModel(
                          invitation
                    )
                }

                invitation.passengerId -> {
                    RideDetailsViewModel.RideDetailsPassengerRequestedViewModel(
                          invitation
                    )
                }

                else -> throw IllegalStateException("invitation id:${invitation.id} has illegal state")
            }
        } else if (invitation.cancelledAt == null && invitation.confirmedAt != null) {
            RideDetailsViewModel.RideDetailsPassengerConfirmedViewModel(
                  invitation
            )
        } else if (invitation.cancelledAt != null) {
            RideDetailsViewModel.RideDetailsCancelModel(invitation)
        } else {
            throw IllegalStateException("invitation id:${invitation.id} has illegal state which we didn't expected")
        }
    }

    private fun translateDriverModels(rideForDetails: RideForDetails): List<RideDetailsViewModel> {
        val list: MutableList<RideDetailsViewModel> = mutableListOf()

        rideForDetails.invitations.forEach { invitation ->
            list.add(translateInvitation(rideForDetails.role, invitation))
        }

        rideForDetails.recommendations.forEach { recommendation ->

            list.add(
                  RideDetailsViewModel.RideDetailsDriverRecommendationViewModel(
                        recommendation = recommendation
                  )
            )
        }

        return list
    }

    private fun translatePassengerModels(rideForDetails: RideForDetails): List<RideDetailsViewModel> {
        val list: MutableList<RideDetailsViewModel> = mutableListOf()

        rideForDetails.invitations.forEach { invitation ->
            list.add(translateInvitation(rideForDetails.role, invitation))
        }

        rideForDetails.recommendations.forEach { recommendation ->
            list.add(
                  RideDetailsViewModel.RideDetailsPassengerRecommendationViewModel(
                        recommendation = recommendation
                  )
            )
        }

        return list
    }
}
