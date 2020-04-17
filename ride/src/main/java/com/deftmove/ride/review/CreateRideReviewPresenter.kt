package com.deftmove.ride.review

import com.deftmove.carpooling.interfaces.OpenRideFeedOrRegistrationScreen
import com.deftmove.carpooling.interfaces.ride.create.model.CreateRideModel
import com.deftmove.carpooling.interfaces.ride.create.model.CreateRideReviewModel
import com.deftmove.carpooling.interfaces.ride.create.model.RepeatingRideModel
import com.deftmove.carpooling.interfaces.ride.service.RideService
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.heart.common.extension.addSeconds
import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonAction
import com.deftmove.heart.interfaces.common.presenter.PresenterView
import com.deftmove.heart.interfaces.common.rx.doOnData
import com.deftmove.heart.interfaces.map.Location
import com.deftmove.heart.interfaces.model.SearchAddressPrediction
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import timber.log.Timber
import java.util.Date

class CreateRideReviewPresenter(
    private val currentUserManager: CurrentUserManager,
    private val rideService: RideService,
    private val heartNavigator: HeartNavigator,
    private val reactiveTransformer: ReactiveTransformer
) : Presenter<CreateRideReviewPresenter.View>() {

    private lateinit var currentCreateRideModel: CreateRideModel

    override fun initialise() {
        currentCreateRideModel = view?.getReceivedParams()!!.model

        commonView?.actions()
              ?.subscribeOn(reactiveTransformer.ioScheduler())
              ?.subscribeSafeWithShowingErrorContent { action ->
                  Timber.d("action:%s", action)
                  when (action) {
                      is PresenterCommonAction.ErrorRetryClicked -> {
                          commonView?.showContent()
                      }

                      Action.SubmitClicked -> {
                          if (currentCreateRideModel.isEditing) {
                              rideService.updateRideAsDriver(
                                    id = currentCreateRideModel.editRideId!!,
                                    updateRepeatingRides = currentCreateRideModel.updateRepeatingRides,
                                    origin = currentCreateRideModel.origin.location,
                                    destination = currentCreateRideModel.destination.location,
                                    startTime = currentCreateRideModel.startTime,
                                    route = currentCreateRideModel.route!!,
                                    repeat = currentCreateRideModel.repeat
                              )
                                    .doOnSubscribe { commonView?.showContentLoading() }
                                    .subscribeOn(reactiveTransformer.ioScheduler())
                                    .doOnData {
                                        heartNavigator.getLauncher(
                                              OpenRideFeedOrRegistrationScreen
                                        )?.startActivity()
                                    }
                                    .subscribeSafeResponseWithShowingErrorContent()
                          } else {
                              rideService.createRideAsDriver(
                                    origin = currentCreateRideModel.origin.location,
                                    destination = currentCreateRideModel.destination.location,
                                    startTime = currentCreateRideModel.startTime,
                                    route = currentCreateRideModel.route,
                                    repeat = currentCreateRideModel.repeat
                              )
                                    .doOnSubscribe { commonView?.showContentLoading() }
                                    .subscribeOn(reactiveTransformer.ioScheduler())
                                    .doOnData {
                                        heartNavigator.getLauncher(
                                              OpenRideFeedOrRegistrationScreen
                                        )?.startActivity()
                                    }
                                    .subscribeSafeResponseWithShowingErrorContent()
                          }
                      }
                  }
              }

        initialForCreateOrEdit()
    }

    private fun initialForCreateOrEdit() {

        if (currentCreateRideModel.isEditing) {
            view?.showSaveButton()
        }

        currentUserManager.getUserModel()?.let { userModel ->
            view?.showCarDetails(userModel.carModel, userModel.carLicensePlate)
        }
        view?.showStartTime(currentCreateRideModel.startTime)
        view?.showRepeating(currentCreateRideModel.repeat)

        updateMap()
    }

    private fun updateMap() {
        currentCreateRideModel.route!!.polyline.let { view?.drawRoute(it) }

        view?.drawOriginPin(currentCreateRideModel.origin.location)
        view?.drawDestinationPin(currentCreateRideModel.destination.location)
        view?.drawWaypointPins(currentCreateRideModel.route!!.stops)

        view?.showOriginAddress(currentCreateRideModel.origin, currentCreateRideModel.startTime)
        view?.showDestinationAddress(
              currentCreateRideModel.destination,
              currentCreateRideModel.startTime
                    .addSeconds(currentCreateRideModel.route!!.duration.toInt())
        )

        view?.showPickupPointAddresses(
              stops = currentCreateRideModel.route!!.stops.mapIndexed { index, prediction ->
                  prediction to calculateDurationTillIndex(index)
              }
        )
    }

    private fun calculateDurationTillIndex(index: Int): Date {
        return currentCreateRideModel.startTime
              .addSeconds(currentCreateRideModel.route!!.stops.take(index + 1).map { it.duration }.sum().toInt())
    }

    interface View : PresenterView {
        fun getReceivedParams(): CreateRideReviewModel

        fun showStartTime(startTime: Date)
        fun showRepeating(repeatingRideModel: RepeatingRideModel)
        fun showCarDetails(carModel: String?, carLicensePlate: String?)

        fun drawRoute(polygon: String)
        fun drawOriginPin(origin: Location)
        fun drawDestinationPin(destination: Location)
        fun drawWaypointPins(waypoints: List<Location>)
        fun showOriginAddress(origin: SearchAddressPrediction, time: Date)
        fun showDestinationAddress(destination: SearchAddressPrediction, time: Date)
        fun showPickupPointAddresses(stops: List<Pair<Location, Date>>)
        fun showSaveButton()
        fun disableSubmitButton()
        fun showMustUpdateCarDetailTitle()
    }

    sealed class Action : PresenterAction {
        object SubmitClicked : Action()
    }
}
