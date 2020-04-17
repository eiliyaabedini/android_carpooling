package com.deftmove.ride.review

import com.deftmove.carpooling.interfaces.OpenRideFeedOrRegistrationScreen
import com.deftmove.carpooling.interfaces.authentication.model.UserModel
import com.deftmove.carpooling.interfaces.ride.create.model.CreateRideModel
import com.deftmove.carpooling.interfaces.ride.create.model.CreateRideReviewModel
import com.deftmove.carpooling.interfaces.ride.create.model.RepeatingRideModel
import com.deftmove.carpooling.interfaces.ride.model.RideForFeed
import com.deftmove.carpooling.interfaces.ride.service.RideService
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.heart.common.extension.toResponseResult
import com.deftmove.heart.interfaces.ResponseResult
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonView
import com.deftmove.heart.interfaces.map.Coordinate
import com.deftmove.heart.interfaces.map.Location
import com.deftmove.heart.interfaces.map.Route
import com.deftmove.heart.interfaces.model.SearchAddressPrediction
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import com.deftmove.heart.testhelper.TestReactiveTransformer
import com.deftmove.heart.testhelper.navigator.ActivityLauncherOpenTest
import com.deftmove.heart.testhelper.thenJust
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import java.util.Date

class CreateRideReviewPresenterTest {

    private val actions: PublishSubject<PresenterAction> = PublishSubject.create()
    private val actionsCommon: PublishSubject<PresenterCommonAction> = PublishSubject.create()

    private val mockUser: UserModel = mock {
        on { carModel } doReturn "user_car_model"
        on { carLicensePlate } doReturn "user_car_license_plate"
    }
    private val mockCurrentUserManager: CurrentUserManager = mock {
        on { getUserModel() } doReturn mockUser
    }

    private val mockHeartNavigator: HeartNavigator = mock()

    private val rideService: RideService = mock()
    private val reactiveTransformer: ReactiveTransformer = TestReactiveTransformer()

    private val presenter: CreateRideReviewPresenter = CreateRideReviewPresenter(
          currentUserManager = mockCurrentUserManager,
          rideService = rideService,
          heartNavigator = mockHeartNavigator,
          reactiveTransformer = reactiveTransformer
    )

    private val mockStops: List<Location> = listOf(
          mock(), mock(), mock(), mock()
    )

    private val mockRoute: Route = mock {
        on { polyline } doReturn "mock_route_polyline"
        on { stops } doReturn mockStops
    }

    private val mockStartTime: Date = Date()
    private val mockRepeatingRideModel: RepeatingRideModel = RepeatingRideModel()

    private val mockReceivedRideForCreate: CreateRideReviewModel = CreateRideReviewModel(
          model = CreateRideModel(
                origin = SearchAddressPrediction(
                      Location(
                            0.0, "locationAName", "locationAAddress",
                            Coordinate(1.0, 2.0)
                      ), false
                ),
                destination = SearchAddressPrediction(
                      Location(
                            0.0, "locationBName", "locationBAddress",
                            Coordinate(3.0, 4.0)
                      ), false
                ),
                route = mockRoute,
                startTime = mockStartTime,
                repeat = mockRepeatingRideModel,
                editRideId = "mockEditRideId"
          )
    )

    private val mockView: CreateRideReviewPresenter.View = mock {
        on { getReceivedParams() } doReturn mockReceivedRideForCreate
    }

    private val mockCommonView: PresenterCommonView = mock {
        on(it.actions()) doReturn (actions.mergeWith(actionsCommon))
    }

    //    @Before
    //    fun setUp() {
    //        Timber.plant(TestDebugTree())
    //    }

    @Test
    fun `when retry in error screen clicked then show contents again`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        reset(mockCommonView)

        actions.onNext(PresenterCommonAction.ErrorRetryClicked)

        verify(mockCommonView).showContent()
    }

    @Test
    fun `when initialize then get received ride`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        verify(mockView).getReceivedParams()
    }

    @Test
    fun `when initialize then show user car details`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        verify(mockCurrentUserManager).getUserModel()
        verify(mockView).showCarDetails(carModel = "user_car_model", carLicensePlate = "user_car_license_plate")
    }

    @Test
    fun `when initialize then show start time`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        verify(mockView).showStartTime(mockStartTime)
    }

    @Test
    fun `when initialize then show repeating`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        verify(mockView).showRepeating(mockRepeatingRideModel)
    }

    @Test
    fun `when initialize then draw origin and destination`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        verify(mockView).showOriginAddress(
              mockReceivedRideForCreate.model.origin,
              mockReceivedRideForCreate.model.startTime
        )
        verify(mockView).showDestinationAddress(
              mockReceivedRideForCreate.model.destination,
              mockReceivedRideForCreate.model.startTime
        )
    }

    @Test
    fun `when initialize then draw the route`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        verify(mockView).drawRoute("mock_route_polyline")
    }

    @Test
    fun `when initialize then draw the origin pin`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        verify(mockView).drawOriginPin(mockReceivedRideForCreate.model.origin.location)
    }

    @Test
    fun `when initialize then draw the destination pin`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        verify(mockView).drawDestinationPin(mockReceivedRideForCreate.model.destination.location)
    }

    @Test
    fun `when initialize then draw the stop pins`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        verify(mockView).drawWaypointPins(mockReceivedRideForCreate.model.route!!.stops)
    }

    @Test
    fun `when initialize then draw pickup points`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        verify(mockView).showPickupPointAddresses(mockStops.map { it to mockStartTime })
    }

    @Test
    fun `when create button clicked then ask backend to create a driver ride`() {
        whenever(mockView.getReceivedParams()).thenReturn(mockReceivedRideForCreate)
        whenever(rideService.createRideAsDriver(any(), any(), any(), any(), any()))
              .thenJust(mock<RideForFeed>().toResponseResult())

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        reset(mockCommonView)

        actions.onNext(CreateRideReviewPresenter.Action.SubmitClicked)

        verify(rideService).createRideAsDriver(
              origin = mockReceivedRideForCreate.model.origin.location,
              destination = mockReceivedRideForCreate.model.destination.location,
              startTime = mockStartTime,
              route = mockReceivedRideForCreate.model.route,
              repeat = mockReceivedRideForCreate.model.repeat
        )
    }

    @Test
    fun `when is editing and save button is clicked then update driver ride`() {
        whenever(mockView.getReceivedParams()).thenReturn(
              mockReceivedRideForCreate.copy(
                    model = mockReceivedRideForCreate.model.copy(
                          isEditing = true
                    )
              )
        )
        whenever(rideService.updateRideAsDriver(any(), anyOrNull(), any(), any(), any(), any(), any()))
              .thenJust(mock<RideForFeed>().toResponseResult())

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        reset(mockCommonView)

        actions.onNext(CreateRideReviewPresenter.Action.SubmitClicked)

        verify(rideService).updateRideAsDriver(
              id = mockReceivedRideForCreate.model.editRideId!!,
              updateRepeatingRides = mockReceivedRideForCreate.model.updateRepeatingRides,
              origin = mockReceivedRideForCreate.model.origin.location,
              destination = mockReceivedRideForCreate.model.destination.location,
              startTime = mockStartTime,
              route = mockReceivedRideForCreate.model.route,
              repeat = mockReceivedRideForCreate.model.repeat
        )
    }

    @Test
    fun `when create button clicked then show loading while creating ride`() {
        whenever(rideService.createRideAsDriver(any(), any(), any(), any(), any()))
              .thenJust(mock<RideForFeed>().toResponseResult())

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        reset(mockCommonView)

        actions.onNext(CreateRideReviewPresenter.Action.SubmitClicked)

        verify(mockCommonView).showContentLoading()
    }

    @Test
    fun `when create button clicked and we successfully create a ride then redirect user to ride feed`() {
        whenever(rideService.createRideAsDriver(any(), any(), any(), any(), any()))
              .thenJust(mock<RideForFeed>().toResponseResult())

        val launcherModel = ActivityLauncherOpenTest()
        whenever(mockHeartNavigator.getLauncher(OpenRideFeedOrRegistrationScreen))
              .thenReturn(launcherModel)

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        reset(mockCommonView)

        actions.onNext(CreateRideReviewPresenter.Action.SubmitClicked)

        launcherModel.test()
              .startActivityCalled()
    }

    @Test
    fun `when create button clicked and we receive error from backend then show error`() {
        whenever(rideService.createRideAsDriver(any(), any(), any(), any(), any()))
              .thenJust(ResponseResult.Failure(mock<Exception>()))

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        reset(mockCommonView)

        actions.onNext(CreateRideReviewPresenter.Action.SubmitClicked)

        verify(mockCommonView).showContentError()
    }

    @Test
    fun `when user didn't define car detail then disable submit button and show an error`() {
        val mockUser: UserModel = mock {
            on { carModel } doReturn null
            on { carLicensePlate } doReturn null
        }

        whenever(mockCurrentUserManager.getUserModel()).then {  mockUser}

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        // TODO check for disable submit button invoked
        // TODO check for show appropriate error
    }

    @Test
    fun `when user has defined car detail then don't disable submit button`() {
        val mockUser: UserModel = mock {
            on { carModel } doReturn null
            on { carLicensePlate } doReturn null
        }

        whenever(mockCurrentUserManager.getUserModel()).then {  mockUser}

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        // TODO check for disable submit button not invoked
    }
}
