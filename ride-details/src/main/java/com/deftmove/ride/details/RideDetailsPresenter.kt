package com.deftmove.ride.details

import com.deftmove.carpooling.interfaces.OpenProfilePublicScreen
import com.deftmove.carpooling.interfaces.authentication.model.UserModel
import com.deftmove.carpooling.interfaces.invitation.InvitationService
import com.deftmove.carpooling.interfaces.pushnotification.model.PushNotificationEvents
import com.deftmove.carpooling.interfaces.ride.details.model.RideDetailsActivityModel
import com.deftmove.carpooling.interfaces.ride.details.service.RideDetailsService
import com.deftmove.carpooling.interfaces.ride.model.RideRole
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.heart.common.event.EventManager
import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonAction
import com.deftmove.heart.interfaces.common.presenter.PresenterView
import com.deftmove.heart.interfaces.common.rx.doOnData
import com.deftmove.heart.interfaces.map.Location
import com.deftmove.heart.interfaces.map.toLocation
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import com.deftmove.ride.details.model.ConfirmedPassengerMapMarkerModel
import com.deftmove.ride.details.model.RideDetailsViewModel
import timber.log.Timber
import java.util.concurrent.TimeUnit

class RideDetailsPresenter(
    private val rideDetailsService: RideDetailsService,
    private val invitationService: InvitationService,
    private val currentUserManager: CurrentUserManager,
    private val eventManager: EventManager,
    private val heartNavigator: HeartNavigator,
    private val reactiveTransformer: ReactiveTransformer
) : Presenter<RideDetailsPresenter.View>() {

    private lateinit var currentRole: RideRole
    private lateinit var currentRideId: String
    private lateinit var currentRideOrigin: Location
    private lateinit var currentRideDestination: Location
    private var areConfirmedPassengersDrawnOnMap = false
    private val viewModelItems: MutableList<RideDetailsViewModel> = mutableListOf()
    override fun initialise() {
        commonView?.actions()
              ?.subscribeOn(reactiveTransformer.ioScheduler())
              ?.subscribeSafeWithShowingErrorContent { action ->
                  Timber.d("action:%s", action)
                  when (action) {

                      is PresenterCommonAction.ErrorRetryClicked -> {
                          commonView?.showContent()
                          fetchRideAndInvitations()
                      }

                      is Action.CloseButtonClicked -> {
                          commonView?.closeScreen()
                      }

                      is Action.AvatarClicked -> {
                          heartNavigator.getLauncher(
                                OpenProfilePublicScreen(action.userModel.id)
                          )?.startActivity()
                      }

                      is Action.ContactClicked -> {
                          action.userModel.phoneNumber?.let { commonView?.callPhoneNumber(it) }
                      }

                      is Action.CreateInvitation -> {
                          when (currentRole) {
                              RideRole.DRIVER -> createInvitation(
                                    offerId = currentRideId,
                                    requestId = action.recommendationId
                              )
                              RideRole.PASSENGER -> createInvitation(
                                    offerId = action.recommendationId,
                                    requestId = currentRideId
                              )
                          }
                      }

                      is Action.CancelInvitation -> {
                          cancelInvitation(invitationId = action.invitationId)
                      }

                      is Action.ConfirmInvitation -> {
                          confirmInvitation(invitationId = action.invitationId)
                      }

                      is Action.BottomSheetCollapsed -> {
                          areConfirmedPassengersDrawnOnMap = false
                          view?.cleanConfirmedMapElements()
                          drawMapElementsOnCardAppearance(action.index)
                      }

                      is Action.BottomSheetExpanded -> {
                          if (!areConfirmedPassengersDrawnOnMap) {
                              areConfirmedPassengersDrawnOnMap = true
                              view?.cleanMatchesMapElements()
                              val confirmedPassengers: List<RideDetailsViewModel.RideDetailsDriverConfirmedViewModel> =
                                    viewModelItems
                                          .filterIsInstance(RideDetailsViewModel.RideDetailsDriverConfirmedViewModel::class.java)
                              val polylines: List<String> = confirmedPassengers.mapNotNull { it.invitation.sharedRoute }
                              val markerModels: List<ConfirmedPassengerMapMarkerModel> = confirmedPassengers.map {
                                  ConfirmedPassengerMapMarkerModel(
                                        it.invitation.passenger,
                                        it.invitation.pickup?.toLocation()
                                  )
                              }
                              view?.drawConfirmedPassengersSharedRoutesOnMap(polylines)
                              view?.drawConfirmedPassengersPickupPointsOnMap(markerModels)
                          }
                      }
                  }
              }


        commonView?.actions()
              ?.ofType(Action.HorizontalListScrolled::class.java)
              ?.subscribeOn(reactiveTransformer.ioScheduler())
              ?.debounce(DEBOUNCE_DELAY_FOR_HORIZONTAL_SCROLL, TimeUnit.MILLISECONDS)
              ?.distinctUntilChanged()
              ?.subscribeSafeWithShowingErrorContent { action ->
                  Timber.d("HorizontalListScrolled action:%s", action)
                  drawMapElementsOnCardAppearance(action.index)
              }

        eventManager.observe()
              .filter { dataEvent ->
                  dataEvent is PushNotificationEvents.RideNotificationReceived &&
                        dataEvent.rideId == currentRideId
              }
              .doOnNext { fetchRideAndInvitations() }
              .subscribeSafeWithShowingErrorContent()

        fetchRideAndInvitations()
    }

    private fun drawMapElementsOnCardAppearance(index: Int) {
        //filter => driver confirmed passengers are only for bottom sheet
        val otherRide = viewModelItems.filter {
            it !is RideDetailsViewModel.RideDetailsDriverConfirmedViewModel
        }.getOrNull(index)

        when (otherRide) {
            is RideDetailsViewModel.RideDetailsPassengerRecommendationViewModel -> {
                showMapForOtherUser(
                      otherRide.recommendation.user,
                      otherRide.recommendation.role,
                      currentRideOrigin,
                      currentRideDestination,
                      otherRide.recommendation.pickup?.toLocation(),
                      otherRide.recommendation.dropoff?.toLocation(),
                      otherRide.recommendation.sharedRoute
                )
            }

            is RideDetailsViewModel.RideDetailsPassengerConfirmedViewModel -> {
                showMapForOtherUser(
                      otherRide.invitation.driver,
                      RideRole.DRIVER,
                      currentRideOrigin,
                      currentRideDestination,
                      otherRide.invitation.pickup?.toLocation(),
                      otherRide.invitation.dropoff?.toLocation(),
                      otherRide.invitation.sharedRoute
                )
            }

            is RideDetailsViewModel.RideDetailsPassengerRequestedViewModel -> {
                showMapForOtherUser(
                      otherRide.invitation.driver,
                      RideRole.DRIVER,
                      currentRideOrigin,
                      currentRideDestination,
                      otherRide.invitation.pickup?.toLocation(),
                      otherRide.invitation.dropoff?.toLocation(),
                      otherRide.invitation.sharedRoute
                )
            }

            is RideDetailsViewModel.RideDetailsPassengerOfferedViewModel -> {
                showMapForOtherUser(
                      otherRide.invitation.driver,
                      RideRole.DRIVER,
                      currentRideOrigin,
                      currentRideDestination,
                      otherRide.invitation.pickup?.toLocation(),
                      otherRide.invitation.dropoff?.toLocation(),
                      otherRide.invitation.sharedRoute
                )
            }

            is RideDetailsViewModel.RideDetailsDriverRecommendationViewModel -> {
                showMapForOtherUser(
                      otherRide.recommendation.user,
                      otherRide.recommendation.role,
                      currentRideOrigin,
                      currentRideDestination,
                      otherRide.recommendation.pickup?.toLocation(),
                      otherRide.recommendation.dropoff?.toLocation(),
                      otherRide.recommendation.sharedRoute
                )
            }

            is RideDetailsViewModel.RideDetailsDriverRequestedViewModel -> {
                showMapForOtherUser(
                      otherRide.invitation.passenger,
                      RideRole.PASSENGER,
                      currentRideOrigin,
                      currentRideDestination,
                      otherRide.invitation.pickup?.toLocation(),
                      otherRide.invitation.dropoff?.toLocation(),
                      otherRide.invitation.sharedRoute
                )
            }

            is RideDetailsViewModel.RideDetailsDriverOfferedViewModel -> {
                Timber.d("HorizontalListScrolled Offered Model")
                showMapForOtherUser(
                      otherRide.invitation.passenger,
                      RideRole.PASSENGER,
                      currentRideOrigin,
                      currentRideDestination,
                      otherRide.invitation.pickup?.toLocation(),
                      otherRide.invitation.dropoff?.toLocation(),
                      otherRide.invitation.sharedRoute
                )
            }
        }
    }

    private fun fetchRideAndInvitations() {
        rideDetailsService.getRideDetails(view?.getReceivedParams()!!.rideId)
              .doOnSubscribe { commonView?.showContentLoading() }
              .subscribeOn(reactiveTransformer.ioScheduler())
              .doOnData { rideForDetails ->
                  currentRole = rideForDetails.role
                  currentRideId = rideForDetails.id
                  currentRideOrigin = rideForDetails.origin
                  currentRideDestination = rideForDetails.destination

                  showMapForCurrentUser(
                        currentUserManager.getUserModel()!!,
                        rideForDetails.role,
                        rideForDetails.origin,
                        rideForDetails.destination,
                        rideForDetails.polyline
                  )

                  viewModelItems.clear()
                  viewModelItems.addAll(RideViewModelTranslator.translateModels(rideForDetails))

                  updateList()
              }
              .subscribeSafeResponseWithShowingErrorContent()
    }

    private fun showMapForCurrentUser(
        currentUser: UserModel,
        role: RideRole,
        origin: Location,
        destination: Location,
        polyline: String?
    ) {
        when (role) {
            RideRole.DRIVER -> view?.drawCurrentDriverPinAndRouteInMap(origin, destination, polyline)
            RideRole.PASSENGER -> view?.drawCurrentPassengerPinAndRouteInMap(currentUser, origin, destination, polyline)
        }
    }

    private fun showMapForOtherUser(
        currentUser: UserModel,
        role: RideRole,
        origin: Location?,
        destination: Location?,
        pickupPoint: Location?,
        dropOffPoint: Location?,
        polyline: String?
    ) {
        when (role) {
            RideRole.DRIVER -> view?.drawOtherDriverPinAndRouteInMap(
                  origin,
                  destination,
                  pickupPoint,
                  dropOffPoint,
                  polyline
            )
            RideRole.PASSENGER -> view?.drawOtherPassengerPinAndRouteInMap(currentUser, pickupPoint, dropOffPoint, polyline)
        }
    }

    private fun createInvitation(offerId: String, requestId: String) {
        invitationService.createInvitation(
              offerId, requestId
        )
              .subscribeOn(reactiveTransformer.ioScheduler())
              .doOnData { invitation ->
                  RideViewModelTranslator.replaceRecommendationWithInvitation(
                        list = viewModelItems,
                        role = currentRole,
                        offerId = offerId,
                        requestId = requestId,
                        invitation = invitation
                  )

                  updateList(shouldSortModels = false)
              }
              .subscribeSafeResponseWithShowingErrorContent()
    }

    private fun confirmInvitation(invitationId: String) {
        invitationService.confirmInvitation(invitationId)
              .subscribeOn(reactiveTransformer.ioScheduler())
              .doOnData { invitation ->
                  RideViewModelTranslator.replaceInvitationWithInvitation(
                        list = viewModelItems,
                        role = currentRole,
                        invitationId = invitationId,
                        invitation = invitation
                  )

                  updateList(shouldSortModels = false)
              }
              .subscribeSafeResponseWithShowingErrorContent()
    }

    private fun cancelInvitation(invitationId: String) {
        invitationService.cancelInvitation(invitationId)
              .subscribeOn(reactiveTransformer.ioScheduler())
              .doOnData {

                  viewModelItems.removeAll { it.id == invitationId }

                  updateList(shouldSortModels = false)
              }
              .subscribeSafeResponseWithShowingErrorContent()
    }

    private fun updateList(shouldSortModels: Boolean = true) {
        commonView?.showContent()

        when {
            listIsEmpty() -> {
                view?.cleanMatchesMapElements()
                view?.updateList(
                      when (currentRole) {
                          RideRole.DRIVER -> {
                              listOf(
                                    RideDetailsViewModel.RideDetailsDriverEmptyViewModel(
                                          confirmedPassengerCount = viewModelItems
                                                .filterIsInstance<RideDetailsViewModel.RideDetailsPassengerConfirmedViewModel>()
                                                .count()
                                    )
                              )
                          }

                          RideRole.PASSENGER -> listOf(RideDetailsViewModel.RideDetailsPassengerEmptyViewModel)
                      }
                )
            }

            passengerHasConfirmedDriver() -> view?.updateList(
                  viewModelItems
                        .filterIsInstance<RideDetailsViewModel.RideDetailsPassengerConfirmedViewModel>()
                        .take(1)
            )

            else -> {
                var listModels = viewModelItems
                      .filterNot { it is RideDetailsViewModel.RideDetailsDriverConfirmedViewModel }
                      .filterNot { it is RideDetailsViewModel.RideDetailsCancelModel }
                if (shouldSortModels) {
                    listModels = listModels.sortedBy { RideDetailsViewModel::class.sealedSubclasses.indexOf(it::class) }
                }
                view?.updateList(
                      listModels
                )
            }
        }

        val confirmedPassengers: List<RideDetailsViewModel.RideDetailsDriverConfirmedViewModel> = viewModelItems
              .filterIsInstance(RideDetailsViewModel.RideDetailsDriverConfirmedViewModel::class.java)

        if (confirmedPassengers.isEmpty()) {
            view?.hideConfirmedList()
        } else {
            view?.updateAndShowConfirmedList(confirmedPassengers)
        }
    }

    private fun listIsEmpty(): Boolean {
        return viewModelItems
              .filterNot { it is RideDetailsViewModel.RideDetailsDriverConfirmedViewModel }
              .filterNot { it is RideDetailsViewModel.RideDetailsCancelModel }
              .isEmpty()
    }

    private fun passengerHasConfirmedDriver(): Boolean {
        if (currentRole == RideRole.DRIVER) return false

        return viewModelItems.any { it is RideDetailsViewModel.RideDetailsPassengerConfirmedViewModel }
    }

    interface View : PresenterView {
        fun getReceivedParams(): RideDetailsActivityModel

        fun drawCurrentDriverPinAndRouteInMap(origin: Location, destination: Location, polyline: String?)
        fun drawCurrentPassengerPinAndRouteInMap(
            currentUser: UserModel,
            origin: Location,
            destination: Location,
            polyline: String?
        )

        fun drawOtherDriverPinAndRouteInMap(
            origin: Location?,
            destination: Location?,
            pickupPoint: Location?,
            dropOffPoint: Location?,
            polyline: String?
        )

        fun drawOtherPassengerPinAndRouteInMap(
            currentUser: UserModel,
            pickupPoint: Location?,
            dropOffPoint: Location?,
            polyline: String?
        )

        fun updateList(items: List<RideDetailsViewModel>)
        fun updateAndShowConfirmedList(items: List<RideDetailsViewModel.RideDetailsDriverConfirmedViewModel>)
        fun hideConfirmedList()
        fun cleanMatchesMapElements()
        fun cleanConfirmedMapElements()
        fun drawConfirmedPassengersPickupPointsOnMap(markerModels: List<ConfirmedPassengerMapMarkerModel>)
        fun drawConfirmedPassengersSharedRoutesOnMap(polylines: List<String>)
    }

    sealed class Action : PresenterAction {
        object CloseButtonClicked : Action()
        data class AvatarClicked(val userModel: UserModel) : Action()
        data class ContactClicked(val userModel: UserModel) : Action()
        data class CreateInvitation(val recommendationId: String) : Action()
        data class ConfirmInvitation(val invitationId: String) : Action()
        data class CancelInvitation(val invitationId: String) : Action()
        data class HorizontalListScrolled(val index: Int) : Action()
        data class BottomSheetCollapsed(val index: Int) : Action()
        object BottomSheetExpanded : Action()
    }

    companion object {
        private const val DEBOUNCE_DELAY_FOR_HORIZONTAL_SCROLL: Long = 500
    }
}
