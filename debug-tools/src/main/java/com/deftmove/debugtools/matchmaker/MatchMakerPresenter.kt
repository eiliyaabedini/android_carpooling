package com.deftmove.debugtools.matchmaker

import com.deftmove.carpooling.interfaces.OpenAddressSearch
import com.deftmove.carpooling.interfaces.authentication.network.AuthenticationManager
import com.deftmove.carpooling.interfaces.invitation.InvitationService
import com.deftmove.carpooling.interfaces.ride.create.model.RepeatingRideModel
import com.deftmove.carpooling.interfaces.ride.model.Invitation
import com.deftmove.carpooling.interfaces.ride.model.RideForFeed
import com.deftmove.carpooling.interfaces.ride.service.RideService
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.debugtools.DebugConsts
import com.deftmove.heart.common.event.EventManager
import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.heart.interfaces.ResponseResult
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.heart.interfaces.common.presenter.PresenterView
import com.deftmove.heart.interfaces.common.rx.doOnFailure
import com.deftmove.heart.interfaces.common.rx.flatMapData
import com.deftmove.heart.interfaces.map.Location
import com.deftmove.heart.interfaces.map.Route
import com.deftmove.heart.interfaces.model.SearchAddressPrediction
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import kotlin.collections.set

class MatchMakerPresenter(
    private val currentUserManager: CurrentUserManager,
    private val authenticationManager: AuthenticationManager,
    private val invitationService: InvitationService,
    private val eventManager: EventManager,
    private val rideService: RideService,
    private val heartNavigator: HeartNavigator,
    private val reactiveTransformer: ReactiveTransformer
) : Presenter<MatchMakerPresenter.View>() {

    private var selectedDriver: MockMakerUser = MockMakerUser("", "")
    private lateinit var selectedTripStatus: RideStatus
    private val selectedPassengers = mutableMapOf<Int, MockMakerUser>()
    private val resultLog = mutableListOf<String>()

    private var startTime: Date = Calendar.getInstance().apply {
        time = Date()

        add(Calendar.HOUR_OF_DAY, 2)
    }.time

    private var origin: Location = DebugConsts.Home_Address
    private var destination: Location = DebugConsts.Work_Address

    override fun initialise() {

        view?.setPickedDate(startTime)
        view?.setOrigin(origin)
        view?.setDestination(destination)

        commonView?.actions()
              ?.subscribeOn(reactiveTransformer.ioScheduler())
              ?.subscribeSafeWithShowingErrorContent { action ->
                  Timber.d("action: $action")
                  when (action) {
                      is Action.OriginPickerClicked -> {
                          getOriginAddress()
                      }

                      is Action.DestinationPickerClicked -> {
                          getDestinationAddress()
                      }

                      is Action.DatePickerClicked -> {
                          getTimeFromTimePicker()
                      }

                      is Action.AddMorePax -> {
                          makePassengerSpinner()
                      }

                      is Action.CreateMatches -> {
                          createMatches()
                      }

                      is Action.CloseScreen -> {
                          commonView?.closeScreen()
                      }

                      is Action.ShufflePax -> {
                          getListOfUsers().shuffled()
                                .filter { getListOfUsers().indexOf(it) != 0 }
                                .take(NUMBER_OF_SHUFFLES)
                                .distinct()
                                .forEach {
                                    val index = getListOfUsers().indexOf(it)
                                    makePassengerSpinner(selectedUserIndex = index)
                                    selectedPassengers[index] = it
                                }
                      }

                      is Action.DaxChanged -> {
                          selectedDriver = action.user
                          selectedTripStatus = action.tripStatus
                      }

                      is Action.PaxChanged -> {
                          selectedPassengers[action.index] = action.user
                      }
                  }
              }

        makeDriverSpinner()
        makePassengerSpinner()
    }

    private fun getOriginAddress() {
        heartNavigator.getLauncher(
              OpenAddressSearch()
        )?.startActivityForResult()
              ?.map { it as SearchAddressPrediction }
              ?.doOnSuccess {
                  origin = it.location
                  view?.setOrigin(it.location)
              }
              ?.subscribeSafeWithShowingErrorContent()
    }

    private fun getDestinationAddress() {
        heartNavigator.getLauncher(
              OpenAddressSearch()
        )?.startActivityForResult()
              ?.map { it as SearchAddressPrediction }
              ?.doOnSuccess {
                  destination = it.location
                  view?.setDestination(it.location)
              }
              ?.subscribeSafeWithShowingErrorContent()
    }

    private fun getTimeFromTimePicker() {
        commonView?.showDialogDateTimePicker()
              ?.doOnSuccess {
                  startTime = it
                  view?.setPickedDate(it)
              }
              ?.subscribeSafeWithShowingErrorContent()
    }

    private fun createMatches() {

        commonView?.showContentLoading()
        resultLog.clear()

        val currentToken = authenticationManager.getAuthToken()
        eventManager.enabled = false //disable event manager temporary

        getDaxRoute()
              .subscribeOn(reactiveTransformer.ioScheduler())
              .toObservable()
              .flatMapData { routes ->
                  // Make DAX trip
                  val driver = selectedDriver
                  val daxTrip = makeDaxTrip(driver, routes[0])
                  // Make PAX trip
                  val paxTrips = selectedPassengers.values.filter { it.apiToken.isNotEmpty() }.map { passenger ->
                      val paxTrip = makePaxTrip(passenger)
                      // Make invitation
                      if (driver.apiToken.isNotEmpty() && daxTrip is ResponseResult.Success && paxTrip is ResponseResult.Success) {
                          when (passenger.status) {
                              InvitationStatus.OFFERED -> createInvitation(driver, daxTrip.data.id, paxTrip.data.id)
                              InvitationStatus.REQUESTED -> createInvitation(passenger, daxTrip.data.id, paxTrip.data.id)

                              InvitationStatus.CONFIRMED -> {
                                  val updateDaxTrip = createInvitation(driver, daxTrip.data.id, paxTrip.data.id)
                                  if (updateDaxTrip is ResponseResult.Success<Invitation>) {
                                      confirmInvitation(passenger, updateDaxTrip.data.id)
                                  }
                              }

                              InvitationStatus.RECOMMENDED -> {
                              }
                          }
                      }
                      paxTrip
                  }
                  // Re-emit trips
                  Observable.fromIterable((listOf(daxTrip) + paxTrips).filterNotNull())
              }
              .doOnFailure { commonView?.showContent() }
              .doOnComplete { commonView?.showContent() }
              .toList()
              .doOnSuccess {
                  if (currentToken != null) {
                      currentUserManager.setApiToke(currentToken)
                  }

                  eventManager.enabled = true
              }
              .subscribeSafeWithShowingErrorContent()
    }

    private fun createInvitation(
        userFrom: MockMakerUser,
        daxRideId: String,
        paxRideId: String
    ): ResponseResult<Invitation> {
        currentUserManager.setApiToke(userFrom.apiToken)
        val invitation = invitationService.createInvitation(offerId = daxRideId, requestId = paxRideId).blockingGet()
        when (invitation) {
            is ResponseResult.Success -> resultLog.add("Invite OK from ${userFrom.name}")
            is ResponseResult.Failure -> resultLog.add(
                  "Invite Failed from ${userFrom.name}"
            )
        }
        return invitation
    }

    private fun confirmInvitation(userFrom: MockMakerUser, invitationId: String): ResponseResult<Invitation> {
        currentUserManager.setApiToke(userFrom.apiToken)
        val invitation = invitationService.confirmInvitation(invitationId = invitationId).blockingGet()
        when (invitation) {
            is ResponseResult.Success -> resultLog.add("Invite OK from ${userFrom.name}")
            is ResponseResult.Failure -> resultLog.add(
                  "Invite Failed from ${userFrom.name}"
            )
        }
        return invitation
    }

    private fun makeDaxTrip(driver: MockMakerUser, route: Route): ResponseResult<RideForFeed>? {
        if (driver.apiToken.isEmpty()) return null

        currentUserManager.setApiToke(driver.apiToken)
        val daxTrip = rideService.createRideAsDriver(
              origin = origin,
              destination = destination,
              startTime = startTime,
              route = route,
              repeat = RepeatingRideModel()
        ).blockingGet()
        when (daxTrip) {
            is ResponseResult.Success -> resultLog.add("Created DAX: ${driver.name}")
            is ResponseResult.Failure -> resultLog.add("Failed DAX: ${driver.name}")
        }
        return daxTrip
    }

    private fun makePaxTrip(user: MockMakerUser): ResponseResult<RideForFeed> {
        currentUserManager.setApiToke(user.apiToken)
        val paxTrip = rideService.createRideAsPassenger(
              origin = origin,
              destination = destination,
              startTime = startTime,
              repeat = RepeatingRideModel()
        ).blockingGet()

        when (paxTrip) {
            is ResponseResult.Success -> resultLog.add("Created PAX: ${user.name}")
            is ResponseResult.Failure -> resultLog.add("Failed PAX: ${user.name}")
        }

        return paxTrip
    }

    private fun getDaxRoute(): Single<ResponseResult<List<Route>>> {
        return rideService.findRoutes(
              origin = origin,
              destination = destination,
              stops = emptyList(),
              startTime = startTime
        )
    }

    private fun makePassengerSpinner(index: Int = selectedPassengers.size + 1, selectedUserIndex: Int = 0) {
        selectedPassengers[index] = MockMakerUser("", "")
        view?.makePassengerSpinner(getListOfUsers(), index, selectedUserIndex)
    }

    private fun makeDriverSpinner() {
        view?.makeDriverSpinner(getListOfUsers())
    }

    private fun getListOfUsers(): List<MockMakerUser> {
        return DebugConsts.accounts.map {
            MockMakerUser(
                  it.key,
                  it.value
            )
        }
    }

    interface View : PresenterView {

        fun makeDriverSpinner(list: List<MockMakerUser>)
        fun makePassengerSpinner(list: List<MockMakerUser>, index: Int, selectedUserIndex: Int)

        fun setPickedDate(date: Date)
        fun setOrigin(location: Location)
        fun setDestination(location: Location)
    }

    sealed class Action : PresenterAction {
        object DatePickerClicked : Action()
        object OriginPickerClicked : Action()
        object DestinationPickerClicked : Action()
        object AddMorePax : Action()
        object CreateMatches : Action()
        object CloseScreen : Action()
        object ShufflePax : Action()

        class PaxChanged(val index: Int, val user: MockMakerUser) : Action()
        class DaxChanged(val tripStatus: RideStatus, val user: MockMakerUser) : Action()
    }

    companion object {
        private const val NUMBER_OF_SHUFFLES: Int = 10
    }
}

data class MockMakerUser(
    val name: String, val apiToken: String,
    val status: InvitationStatus = InvitationStatus.RECOMMENDED
)

enum class InvitationStatus {
    RECOMMENDED,
    OFFERED,
    REQUESTED,
    CONFIRMED
}

enum class RideStatus {
    SCHEDULED,
    STARTED
}
