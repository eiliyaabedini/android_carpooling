package com.deftmove.ride.create

import com.deftmove.carpooling.interfaces.OpenAddressSearch
import com.deftmove.carpooling.interfaces.OpenRepeatingScreen
import com.deftmove.carpooling.interfaces.OpenWayPointsScreen
import com.deftmove.carpooling.interfaces.ride.create.model.CreateRideLaunchModel
import com.deftmove.carpooling.interfaces.ride.create.model.CreateRideModel
import com.deftmove.carpooling.interfaces.ride.create.model.RepeatingRideModel
import com.deftmove.carpooling.interfaces.ride.model.RideForFeed
import com.deftmove.carpooling.interfaces.ride.model.RideRole
import com.deftmove.carpooling.interfaces.ride.service.RideService
import com.deftmove.heart.common.extension.ToResponseResultSingle
import com.deftmove.heart.interfaces.ResponseResult
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.TextUtils
import com.deftmove.heart.interfaces.common.model.Money
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonView
import com.deftmove.heart.interfaces.map.Coordinate
import com.deftmove.heart.interfaces.map.Location
import com.deftmove.heart.interfaces.map.service.CurrentLocation
import com.deftmove.heart.interfaces.model.SearchAddressPrediction
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import com.deftmove.heart.testhelper.TestReactiveTransformer
import com.deftmove.heart.testhelper.navigator.ActivityLauncherOpenTest
import com.deftmove.resources.R
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Maybe
import io.reactivex.subjects.PublishSubject
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentCaptor
import java.util.Date

class CreateRidePresenterTest {

    private val currentLocation: CurrentLocation = mock {
        on { getCurrentLocation(any()) } doReturn Maybe.empty()
    }

    private val mockedDate = Date()
    private val mockRepeatingRideModel = RepeatingRideModel()

    private val originLocation = Location(
          duration = 1.0,
          name = "origin_name",
          address = "origin_address",
          coordinate = Coordinate(latitude = 1.01, longitude = 1.02)
    )

    private val destinationLocation = Location(
          duration = 2.0,
          name = "destination_name",
          address = "destination_address",
          coordinate = Coordinate(latitude = 2.01, longitude = 2.02)
    )
    private val mockOriginLocation = SearchAddressPrediction(
          location = originLocation, isCurrentLocation = true
    )

    private val mockDestinationLocation = SearchAddressPrediction(
          location = destinationLocation, isCurrentLocation = false
    )

    private val mockDriverRideForFeed = RideForFeed(
          id = "mocked_ID",
          origin = originLocation,
          destination = destinationLocation,
          role = RideRole.PASSENGER,
          repeat = mockRepeatingRideModel,
          time = mockedDate,
          recommendationsCount = 1,
          offeredCount = 0,
          requestedCount = 0,
          cancelledCount = 0,
          sumConfirmedPrice = Money.EMPTY,
          declinedCount = null,
          driver = null,
          route = null,
          confirmedCount = 0
    )

    private val rideService: RideService = mock {
        on { createRideAsPassenger(any(), any(), any(), any()) } doReturn mockDriverRideForFeed.ToResponseResultSingle()
        on {
            updateRideAsPassenger(
                  any(),
                  anyOrNull(),
                  any(),
                  any(),
                  any(),
                  any()
            )
        } doReturn mockDriverRideForFeed.ToResponseResultSingle()

    }
    private val textUtils: TextUtils = mock {
        on { getString(R.string.common_from) } doReturn "from"
        on { getString(R.string.common_to) } doReturn "to"
    }

    private val mockCreateRideLaunchModel = CreateRideLaunchModel(RideRole.PASSENGER, null, null)

    private val heartNavigator: HeartNavigator = mock()

    private val reactiveTransformer: ReactiveTransformer = TestReactiveTransformer()

    private val actions: PublishSubject<PresenterAction> = PublishSubject.create()
    private val actionsCommon: PublishSubject<PresenterCommonAction> = PublishSubject.create()

    private val mockView: CreateRidePresenter.View = mock {
        on { getCreateRideLaunchModel() } doReturn mockCreateRideLaunchModel
    }

    private val mockCommonView: PresenterCommonView = mock {
        on(it.actions()) doReturn (actions.mergeWith(actionsCommon))
        on { getCurrentScope() } doReturn mock()
        on { showDialogDatePicker(any(), any()) } doReturn Maybe.just(mock())
        on { showDialogTimePicker(any()) } doReturn Maybe.just(mock())
    }

    private val presenter = CreateRidePresenter(
          currentLocation, rideService, textUtils, heartNavigator, reactiveTransformer
    )

    @Test
    fun `when initialising then setViewValues has to be called`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        verify(mockView).setViewValues(any())
    }

    @Test
    fun `when click error retry then show content`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actionsCommon.onNext(PresenterCommonAction.ErrorRetryClicked)

        verify(mockCommonView).showContent()
    }

    @Test
    fun `when from location is clicked then open address search screen`() {
        val fromOpenAddressActivityLauncher = ActivityLauncherOpenTest()
        whenever(heartNavigator.getLauncher(OpenAddressSearch(addressTypeName = "from")))
              .thenReturn(fromOpenAddressActivityLauncher.apply {
                  setActivityResult(Maybe.just(mockOriginLocation))
              })

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actions.onNext(CreateRidePresenter.Action.FromLocationClicked)

        fromOpenAddressActivityLauncher.test()
              .startActivityForResultCalled()
    }

    @Test
    fun `when to location is clicked then open address search screen`() {
        val toOpenAddressActivityLauncher = ActivityLauncherOpenTest()
        whenever(
              heartNavigator.getLauncher(
                    OpenAddressSearch(addressTypeName = "to")
              )
        ).thenReturn(
              toOpenAddressActivityLauncher.apply {
                  setActivityResult(Maybe.just(mockDestinationLocation))
              }
        )

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actions.onNext(CreateRidePresenter.Action.ToLocationClicked)

        toOpenAddressActivityLauncher.test()
              .startActivityForResultCalled()
    }

    @Test
    fun `when timeDay is clicked then show DialogDatePicker`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actions.onNext(CreateRidePresenter.Action.TimeDayClicked)

        verify(mockCommonView).showDialogDatePicker(any(), any())
    }

    @Test
    fun `when timeTime is clicked then show DialogTimePicker`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actions.onNext(CreateRidePresenter.Action.TimeTimeClicked)

        verify(mockCommonView).showDialogTimePicker(any())
    }

    @Test
    fun `when continue is clicked and role is driver then navigate to way points screen`() {
        whenever(mockView.getCreateRideLaunchModel()).thenReturn(mockCreateRideLaunchModel.copy(role = RideRole.DRIVER))
        val waypointActivityLauncher = ActivityLauncherOpenTest()
        whenever(heartNavigator.getLauncher<OpenWayPointsScreen>(any())).thenReturn(waypointActivityLauncher)

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actions.onNext(CreateRidePresenter.Action.ContinueClicked)

        waypointActivityLauncher.test()
              .startActivityCalled()
    }

    @Test
    fun `when continue is clicked then create ride as passenger if role is passenger and is not edit mode`() {
        whenever(mockView.getCreateRideLaunchModel()).thenReturn(
              mockCreateRideLaunchModel.copy(
                    role = RideRole.PASSENGER,
                    rideForUpdate = null,
                    updateRepeatingRides = null
              )
        )

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actions.onNext(CreateRidePresenter.Action.ContinueClicked)

        verify(rideService).createRideAsPassenger(any(), any(), any(), any())
    }

    @Test
    fun `when continue is clicked then create ride as passenger if role is passenger and is edit mode`() {
        whenever(mockView.getCreateRideLaunchModel()).thenReturn(
              mockCreateRideLaunchModel.copy(
                    role = RideRole.PASSENGER,
                    rideForUpdate = mockDriverRideForFeed,
                    updateRepeatingRides = null
              )
        )

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actions.onNext(CreateRidePresenter.Action.ContinueClicked)

        verify(rideService).updateRideAsPassenger(
              id = "mocked_ID",
              updateRepeatingRides = null,
              origin = originLocation,
              destination = destinationLocation,
              startTime = mockedDate,
              repeat = mockRepeatingRideModel
        )
    }

    @Test
    fun `when repeating is clicked then navigate to repeating screen`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actions.onNext(CreateRidePresenter.Action.RepeatingClicked)

        verify(heartNavigator).getLauncher(OpenRepeatingScreen(mockRepeatingRideModel))?.startActivityForResult()
    }

    @Test
    fun `when swap locations is clicked then swap currentModel origin with destination`() {
        whenever(heartNavigator.getLauncher(OpenAddressSearch(addressTypeName = "from")))
              .thenReturn(
                    ActivityLauncherOpenTest().apply {
                        setActivityResult(Maybe.just(mockOriginLocation))
                    })

        whenever(heartNavigator.getLauncher(OpenAddressSearch(addressTypeName = "to")))
              .thenReturn(
                    ActivityLauncherOpenTest().apply {
                        setActivityResult(Maybe.just(mockDestinationLocation))
                    })

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actions.onNext(CreateRidePresenter.Action.FromLocationClicked)
        actions.onNext(CreateRidePresenter.Action.ToLocationClicked)

        reset(mockView)

        actions.onNext(CreateRidePresenter.Action.SwapLocationsClicked)

        val argument: ArgumentCaptor<CreateRideModel> = ArgumentCaptor.forClass(CreateRideModel::class.java)

        verify(mockView).setViewValues(capture(argument))

        Assert.assertEquals(mockDestinationLocation, argument.value.origin)
        Assert.assertEquals(mockOriginLocation, argument.value.destination)
    }

    @Test
    fun `when we receive current location then set current location`() {
        val subject: PublishSubject<ResponseResult<Location>> = PublishSubject.create()

        whenever(currentLocation.getCurrentLocation(any())).thenReturn(subject.firstElement())

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        reset(mockView)

        subject.onNext(ResponseResult.Success(destinationLocation))

        val argument: ArgumentCaptor<CreateRideModel> = ArgumentCaptor.forClass(CreateRideModel::class.java)

        verify(mockView).setViewValues(capture(argument))

        Assert.assertEquals(destinationLocation, argument.value.origin.location)
    }

    @Test
    fun `when user click on from location after result of current location then set from location`() {
        whenever(heartNavigator.getLauncher(OpenAddressSearch(addressTypeName = "from")))
              .thenReturn(ActivityLauncherOpenTest().apply {
                  setActivityResult(Maybe.just(mockOriginLocation))
              })

        val subject: PublishSubject<ResponseResult<Location>> = PublishSubject.create()

        whenever(currentLocation.getCurrentLocation(any())).thenReturn(subject.firstElement())

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        subject.onNext(ResponseResult.Success(destinationLocation))

        reset(mockView)

        actions.onNext(CreateRidePresenter.Action.FromLocationClicked)

        val argument: ArgumentCaptor<CreateRideModel> = ArgumentCaptor.forClass(CreateRideModel::class.java)

        verify(mockView).setViewValues(capture(argument))

        Assert.assertEquals(mockOriginLocation, argument.value.origin)
    }

    @Test
    fun `when user click on from location then current location request must be canceled`() {
        val fromOpenAddressActivityLauncher = ActivityLauncherOpenTest().apply {
            setActivityResult(Maybe.just(mockOriginLocation))
        }

        whenever(heartNavigator.getLauncher(OpenAddressSearch(addressTypeName = "from")))
              .thenReturn(fromOpenAddressActivityLauncher)

        val subject: PublishSubject<ResponseResult<Location>> = PublishSubject.create()

        whenever(currentLocation.getCurrentLocation(any())).thenReturn(subject.firstElement())

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actions.onNext(CreateRidePresenter.Action.FromLocationClicked)

        reset(mockView)

        subject.onNext(ResponseResult.Success(destinationLocation))

        verify(mockView, never()).setViewValues(any())
    }
}

fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()
