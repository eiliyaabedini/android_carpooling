package com.deftmove.services.invitation

import com.apollographql.apollo.ApolloClient
import com.deftmove.carpooling.interfaces.invitation.InvitationService
import com.deftmove.carpooling.interfaces.pushnotification.model.PushNotificationEvents
import com.deftmove.carpooling.interfaces.ride.model.Invitation
import com.deftmove.carpooling.interfaces.service.rx.AsyncApollo
import com.deftmove.carpooling.ride.details.CancelInvitationMutation
import com.deftmove.carpooling.ride.details.ConfirmInvitationMutation
import com.deftmove.carpooling.ride.details.CreateInvitationMutation
import com.deftmove.carpooling.type.CancelInvitationInput
import com.deftmove.carpooling.type.ConfirmInvitationInput
import com.deftmove.carpooling.type.CreateInvitationInput
import com.deftmove.heart.common.event.EventManager
import com.deftmove.heart.errorhandler.GenericErrorHandler
import com.deftmove.heart.interfaces.ResponseResult
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.rx.doOnData
import com.deftmove.heart.interfaces.common.rx.doOnFailure
import com.deftmove.heart.interfaces.common.rx.mapData
import com.deftmove.services.extension.convert
import io.reactivex.Single

class InvitationServiceImp(
    private val defaultApolloClient: ApolloClient,
    private val errorHandler: GenericErrorHandler,
    private val eventManager: EventManager,
    private val reactiveTransformer: ReactiveTransformer
) : InvitationService {

    override fun createInvitation(offerId: String, requestId: String): Single<ResponseResult<Invitation>> {
        val mutation = CreateInvitationMutation.builder()
              .input(
                    CreateInvitationInput.builder()
                          .offerId(offerId)
                          .requestId(requestId)
                          .build()
              )
              .build()

        return AsyncApollo.fromSingle(defaultApolloClient.mutate(mutation))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData {
                  it.createInvitation()!!.invitation()!!.fragments().invitationForRideDetailsFragment().convert()
              }
              .doOnData { eventManager.notify(PushNotificationEvents.InvitationCreated(it.id)) }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }

    override fun confirmInvitation(invitationId: String): Single<ResponseResult<Invitation>> {
        val mutation = ConfirmInvitationMutation.builder()
              .input(
                    ConfirmInvitationInput.builder()
                          .id(invitationId)
                          .build()
              )
              .build()

        return AsyncApollo.fromSingle(defaultApolloClient.mutate(mutation))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData {
                  it.confirmInvitation()!!.invitation()!!.fragments().invitationForRideDetailsFragment().convert()
              }
              .doOnData { eventManager.notify(PushNotificationEvents.InvitationConfirmed(it.id)) }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }

    override fun cancelInvitation(invitationId: String): Single<ResponseResult<String>> {
        val mutation = CancelInvitationMutation.builder()
              .input(
                    CancelInvitationInput.builder()
                          .id(invitationId)
                          .build()
              )
              .build()

        return AsyncApollo.fromSingle(defaultApolloClient.mutate(mutation))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData {
                  it.cancelInvitation()!!.invitation()!!.id()
              }
              .doOnData { eventManager.notify(PushNotificationEvents.InvitationCancelled(it)) }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }
}
