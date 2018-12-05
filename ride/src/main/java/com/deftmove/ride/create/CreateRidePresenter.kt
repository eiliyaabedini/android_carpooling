package com.deftmove.ride.create

import com.deftmove.carpooling.interfaces.OpenAddressSearch
import com.deftmove.carpooling.interfaces.OpenRepeatingScreen
import com.deftmove.carpooling.interfaces.OpenWayPointsScreen
import com.deftmove.carpooling.interfaces.ride.create.model.CreateRideLaunchModel
import com.deftmove.carpooling.interfaces.ride.create.model.CreateRideModel
import com.deftmove.carpooling.interfaces.ride.create.model.RepeatingRideModel
import com.deftmove.carpooling.interfaces.ride.model.RideRole
import com.deftmove.carpooling.interfaces.ride.service.RideService
import com.deftmove.heart.common.extension.isInPast
import com.deftmove.heart.common.extension.mergeDateAndTime
import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.TextUtils
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonAction
import com.deftmove.heart.interfaces.common.presenter.PresenterView
import com.deftmove.heart.interfaces.common.rx.doOnData
import com.deftmove.heart.interfaces.map.service.CurrentLocation
import com.deftmove.heart.interfaces.model.SearchAddressPrediction
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import com.deftmove.resources.R
import io.reactivex.disposables.Disposable
import timber.log.Timber

class CreateRidePresenter(
    private val currentLocation: CurrentLocation,
    private val rideService: RideService,
    private val textUtils: TextUtils,
    private val heartNavigator: HeartNavigator,
    private val reactiveTransformer: ReactiveTransformer
) : Presenter<CreateRidePresenter.View>() {

    private var currentLocationDisposable: Disposable? = null
    private val currentModel: CreateRideModel = CreateRideModel()

    override fun initialise() {

        currentModel.fillItUp(view?.getCreateRideLaunchModel()!!)

        commonView?.actions()
              ?.subscribeOn(reactiveTransformer.ioScheduler())
              ?.subscribeSafeWithShowingErrorContent { action ->
                  Timber.d("action:%s", action)
                  when (action) {
                      is PresenterCommonAction.ErrorRetryClicked -> {
                          commonView?.showContent()
                      }

                      is Action.FromLocationClicked -> {
                          currentLocationDisposable?.dispose()
                          heartNavigator.getLauncher(
                                OpenAddressSearch(addressTypeName = textUtils.getString(R.string.common_from))
                          )
                                ?.startActivityForResult()
                                ?.map { it as SearchAddressPrediction }
                                ?.doOnSuccess {
                                    setFromLocation(it)
                                }
                                ?.subscribeSafeWithShowingErrorContent()
                      }

                      is Action.ToLocationClicked -> {
                          heartNavigator.getLauncher(
                                OpenAddressSearch(addressTypeName = textUtils.getString(R.string.common_to))
                          )
                                ?.startActivityForResult()
                                ?.map { it as SearchAddressPrediction }
                                ?.doOnSuccess {
                                    currentModel.destination = it
                                    view?.setViewValues(currentModel)

                                    checkFilledValuesAndEnableButtonIfPossible()
                                }
                                ?.subscribeSafeWithShowingErrorContent()
                      }

                      is Action.TimeDayClicked -> {
                          commonView?.showDialogDatePicker(currentModel.startTime, false)
                                ?.doOnSuccess { date ->
                                    currentModel.startTime =
                                          mergeDateAndTime(date, currentModel.startTime)

                                    view?.setViewValues(currentModel)

                                    checkFilledValuesAndEnableButtonIfPossible()
                                }
                                ?.subscribeSafeWithShowingErrorContent()
                      }

                      is Action.TimeTimeClicked -> {
                          commonView?.showDialogTimePicker(currentModel.startTime)
                                ?.doOnSuccess { time ->
                                    currentModel.startTime =
                                          mergeDateAndTime(currentModel.startTime, time)

                                    view?.setViewValues(currentModel)

                                    checkFilledValuesAndEnableButtonIfPossible()
                                }
                                ?.subscribeSafeWithShowingErrorContent()
                      }

                      is Action.ContinueClicked -> {
                          when (currentModel.role) {
                              RideRole.PASSENGER -> {
                                  updateOrCreateRideForPassenger()
                              }

                              RideRole.DRIVER -> {
                                  heartNavigator.getLauncher(
                                        OpenWayPointsScreen(currentModel)
                                  )?.startActivity()
                              }
                          }
                      }

                      is Action.SwapLocationsClicked -> {
                          val fromLocation = currentModel.origin
                          currentModel.origin = currentModel.destination
                          currentModel.destination = fromLocation

                          view?.setViewValues(currentModel)
                      }

                      is Action.RepeatingClicked -> {
                          requestRepeatingByShowingRepeatingScreen()
                      }

                      is Action.RepeatingSwitchCheckChanged -> {
                          if (action.isCurrentlyChecked) {
                              requestRepeatingByShowingRepeatingScreen()
                          } else {
                              currentModel.repeat = RepeatingRideModel()
                          }

                          view?.setViewValues(currentModel)
                      }
                  }
              }


        initialForCreateOrEdit()
    }

    private fun initialForCreateOrEdit() {
        if (currentModel.isEditing) {
            if (currentModel.updateRepeatingRides == false) {
                view?.disableRepeating()
            }

            checkFilledValuesAndEnableButtonIfPossible()
        } else {
            // New Ride
            fetchCurrentLocation()
        }

        view?.setViewValues(currentModel)
    }

    private fun updateOrCreateRideForPassenger() {
        if (currentModel.isEditing) {
            rideService.updateRideAsPassenger(
                  id = currentModel.editRideId!!,
                  updateRepeatingRides = currentModel.updateRepeatingRides,
                  origin = currentModel.origin.location,
                  destination = currentModel.destination.location,
                  startTime = currentModel.startTime,
                  repeat = currentModel.repeat
            )
                  .doOnSubscribe { commonView?.showContentLoading() }
                  .subscribeOn(reactiveTransformer.ioScheduler())
                  .doOnData {
                      commonView?.closeScreen()
                  }
                  .subscribeSafeResponseWithShowingErrorContent()

        } else {
            rideService.createRideAsPassenger(
                  origin = currentModel.origin.location,
                  destination = currentModel.destination.location,
                  startTime = currentModel.startTime,
                  repeat = currentModel.repeat
            )
                  .doOnSubscribe { commonView?.showContentLoading() }
                  .subscribeOn(reactiveTransformer.ioScheduler())
                  .doOnData {
                      commonView?.closeScreen()
                  }
                  .subscribeSafeResponseWithShowingErrorContent()
        }
    }

    private fun requestRepeatingByShowingRepeatingScreen() {
        heartNavigator.getLauncher(
              OpenRepeatingScreen(currentModel.repeat)
        )
              ?.startActivityForResult()
              ?.map { it as RepeatingRideModel }
              ?.doOnSuccess { repeating ->
                  currentModel.repeat = repeating

                  view?.setViewValues(currentModel)
              }
              ?.subscribeSafeWithShowingErrorContent()
    }

    private fun setFromLocation(prediction: SearchAddressPrediction) {
        currentModel.origin = prediction
        view?.setViewValues(currentModel)

        checkFilledValuesAndEnableButtonIfPossible()
    }

    private fun checkFilledValuesAndEnableButtonIfPossible(): Boolean {
        var isValid = true

        if (currentModel.origin.location.isEmpty()) {
            isValid = false
        }

        if (currentModel.destination.location.isEmpty()) {
            isValid = false
        }

        if (currentModel.startTime.isInPast()) {
            isValid = false
        }

        if (isValid) {
            view?.enableContinueButton()
        } else {
            view?.disableContinueButton()
        }

        return isValid
    }

    private fun fetchCurrentLocation() {
        // Todo do we need this error ?! it shows unnecessary error page if we have problem in location
        currentLocationDisposable = currentLocation.getCurrentLocation(commonView?.getCurrentScope()!!)
              .subscribeOn(reactiveTransformer.ioScheduler())
              .doOnData { prediction ->
                  Timber.d("fetchCurrentLocation received the value:$prediction")
                  setFromLocation(SearchAddressPrediction(prediction, true))
              }
              .subscribeSafeResponseWithShowingErrorContent()
    }

    interface View : PresenterView {
        fun getCreateRideLaunchModel(): CreateRideLaunchModel

        fun setViewValues(model: CreateRideModel)
        fun disableRepeating()
        fun enableContinueButton()
        fun disableContinueButton()
    }

    sealed class Action : PresenterAction {
        object FromLocationClicked : Action()
        object ToLocationClicked : Action()
        object TimeDayClicked : Action()
        object TimeTimeClicked : Action()
        object ContinueClicked : Action()
        object SwapLocationsClicked : Action()
        object RepeatingClicked : Action()
        class RepeatingSwitchCheckChanged(val isCurrentlyChecked: Boolean) : Action()
    }
}
