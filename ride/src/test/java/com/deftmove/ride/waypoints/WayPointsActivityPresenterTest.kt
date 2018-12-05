package com.deftmove.ride.waypoints

import com.deftmove.carpooling.interfaces.OpenAddressSearch
import com.deftmove.carpooling.interfaces.OpenCreateReviewScreen
import com.deftmove.carpooling.interfaces.ride.create.model.CreateRideAddWayPointModel
import com.deftmove.carpooling.interfaces.ride.create.model.CreateRideModel
import com.deftmove.carpooling.interfaces.ride.model.RecommendationOnTheFly
import com.deftmove.carpooling.interfaces.ride.service.RideService
import com.deftmove.heart.common.extension.toResponseResult
import com.deftmove.heart.interfaces.ResponseResult
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.TextUtils
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonView
import com.deftmove.heart.interfaces.map.Coordinate
import com.deftmove.heart.interfaces.map.Location
import com.deftmove.heart.interfaces.map.Route
import com.deftmove.heart.interfaces.map.service.LocationFulfiller
import com.deftmove.heart.interfaces.model.SearchAddressPrediction
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import com.deftmove.heart.maptools.mapbuilder.MarkerType
import com.deftmove.heart.testhelper.TestReactiveTransformer
import com.deftmove.heart.testhelper.navigator.ActivityLauncherOpenTest
import com.deftmove.heart.testhelper.thenJust
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Maybe
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import java.util.Date

class WayPointsActivityPresenterTest {

    private val actions: PublishSubject<PresenterAction> = PublishSubject.create()
    private val actionsCommon: PublishSubject<PresenterCommonAction> = PublishSubject.create()
    private val reactiveTransformer: ReactiveTransformer = TestReactiveTransformer()
    private val rideService: RideService = mock()
    private val locationFulfiller: LocationFulfiller = mock()
    private val textUtils: TextUtils = mock()
    private val heartNavigator: HeartNavigator = mock()
    private var mockCurrentRecommendations: LinkedHashMap<String, RecommendationOnTheFly> = linkedMapOf()
    private val activityLauncherOpenTest = ActivityLauncherOpenTest()

    private val presenter: WayPointsActivityPresenter = WayPointsActivityPresenter(
          rideService = rideService,
          locationFulfiller = locationFulfiller,
          textUtils = textUtils,
          heartNavigator = heartNavigator,
          reactiveTransformer = reactiveTransformer
    )

    private val mockRoute: Route = Route(
          distance = 0.0,
          duration = 0.0,
          stopOrder = null,
          stops = listOf(
                Location(
                      5.0, "locationAName5", "locationAAddress5",
                      Coordinate(6.0, 7.0)
                ),
                Location(
                      8.0, "locationAName8", "locationAAddress8",
                      Coordinate(9.0, 10.0)
                )
          ),
          polyline = "polyline strings"
    )

    private val originLocation = SearchAddressPrediction(
          Location(
                0.0, "locationAName0", "locationAAddress0",
                Coordinate(1.0, 2.0)
          ), false
    )

    private val destinationLocation = SearchAddressPrediction(
          Location(
                2.0, "locationBName2", "locationBAddress2",
                Coordinate(3.0, 4.0)
          ), false
    )

    private val mockReceivedRide: CreateRideAddWayPointModel = CreateRideAddWayPointModel(
          model = CreateRideModel(
                origin = originLocation,
                destination = destinationLocation,
                route = mockRoute,
                startTime = Date()
          )
    )

    private val mockLocationFirst: SearchAddressPrediction = mock {
        on { location } doReturn mock()
    }
    private val mockLocationSecond: SearchAddressPrediction = mock {
        on { location } doReturn mock()
    }
    private val mockLocationThird: SearchAddressPrediction = mock {
        on { location } doReturn mock()
    }
    private val mockLocationForth: SearchAddressPrediction = mock {
        on { location } doReturn mock()
    }
    private val mockError: Exception = mock()
    private val mockView: WayPointsActivityPresenter.View = mock()
    private val mockCommonView: PresenterCommonView = mock {
        on(it.actions()) doReturn (actions.mergeWith(actionsCommon))
    }

    //    @Before
    //    fun setUp() {
    //        Timber.plant(TestDebugTree())
    //    }

    @Test
    fun `when retry in error screen clicked then show contents`() {
        whenever(mockView.getReceivedParams()).thenReturn(mockReceivedRide)

        whenever(rideService.findRoutes(any(), any(), any(), any()))
              .thenJust(emptyList<Route>().toResponseResult())

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        reset(mockCommonView)

        actions.onNext(PresenterCommonAction.ErrorRetryClicked)

        verify(mockCommonView).showContent()
    }

    @Test
    fun `when initialize then make button disable and show loading`() {
        whenever(mockView.getReceivedParams()).thenReturn(mockReceivedRide)

        whenever(rideService.findRoutes(any(), any(), any(), any()))
              .thenJust(emptyList<Route>().toResponseResult())

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        verify(mockView).disableActionButton()
        verify(mockCommonView).showContentLoading()
    }

    @Test
    fun `when initialize then get received ride model that passed to the activity and try to fetch routes`() {
        whenever(mockView.getReceivedParams()).thenReturn(
              mockReceivedRide.copy(model = mockReceivedRide.model.copy(route = mockReceivedRide.model.route?.copy(stops = emptyList())))
        )

        whenever(rideService.findRoutes(any(), any(), any(), any()))
              .thenJust(emptyList<Route>().toResponseResult())

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        verify(mockView).getReceivedParams()
        verify(rideService).findRoutes(
              origin = mockReceivedRide.model.origin.location,
              destination = mockReceivedRide.model.destination.location,
              startTime = mockReceivedRide.model.startTime,
              stops = emptyList()
        )
    }

    @Test
    fun `when initialize and editing then try to fetch routes`() {
        whenever(mockView.getReceivedParams()).thenReturn(mockReceivedRide)

        whenever(rideService.findRoutes(any(), any(), any(), any()))
              .thenJust(emptyList<Route>().toResponseResult())

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        verify(rideService).findRoutes(
              origin = mockReceivedRide.model.origin.location,
              destination = mockReceivedRide.model.destination.location,
              startTime = mockReceivedRide.model.startTime,
              stops = mockReceivedRide.model.route!!.stops
        )
    }

    @Test
    fun `when is editing then show all the pins`() {
        whenever(mockView.getReceivedParams()).thenReturn(
              mockReceivedRide.copy(
                    model = mockReceivedRide.model.copy(
                          isEditing = true
                    )
              )
        )

        whenever(rideService.findRoutes(any(), any(), any(), any()))
              .thenJust(emptyList<Route>().toResponseResult())

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        mockReceivedRide.model.route!!.stops.forEach { location ->
            verify(mockView).addPinToMap(any(), eq(location), any())
        }
    }

    @Test
    fun `when init then show origin and destination`() {
        whenever(mockView.getReceivedParams()).thenReturn(mockReceivedRide)

        whenever(rideService.findRoutes(any(), any(), any(), any()))
              .thenJust(emptyList<Route>().toResponseResult())

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        verify(mockView).addPinToMap(any(), eq(originLocation.location), any())
        verify(mockView).addPinToMap(any(), eq(destinationLocation.location), any())
    }

    @Test
    fun `when initialize and we receive no routes from backend then do not draw route and we show contents`() {
        whenever(mockView.getReceivedParams()).thenReturn(mockReceivedRide)

        whenever(rideService.findRoutes(any(), any(), any(), any()))
              .thenJust(emptyList<Route>().toResponseResult())

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        verify(mockView, never()).drawRoute(any(), any())
        verify(mockView, never()).enableActionButton()
        verify(mockCommonView).showContent()
    }

    @Test
    fun `when initialize and we receive one route from backend then draw route and show contents`() {
        whenever(mockView.getReceivedParams()).thenReturn(mockReceivedRide)

        whenever(rideService.findRoutes(any(), any(), any(), any()))
              .thenJust(listOf(mockRoute).toResponseResult())
        whenever(rideService.getRecommendationsOnTheFlyForDriver(any(), any(), any(), any()))
              .thenJust(
                    listOf(
                          RecommendationOnTheFly(
                                Location.EMPTY,
                                "", "", ""
                          )
                    ).toResponseResult()
              )

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        verify(mockView).drawRoute("Route_Main", listOf(mockRoute))
        verify(mockView).enableActionButton()
        verify(mockCommonView).showContent()
    }

    @Test
    fun `when initialize and we receive multiple routes from backend then draw routes`() {
        whenever(mockView.getReceivedParams()).thenReturn(mockReceivedRide)

        whenever(rideService.findRoutes(any(), any(), any(), any()))
              .thenJust(listOf(mockRoute, mockRoute, mockRoute).toResponseResult())
        whenever(rideService.getRecommendationsOnTheFlyForDriver(any(), any(), any(), any()))
              .thenJust(emptyList<RecommendationOnTheFly>().toResponseResult())

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        verify(mockView).drawRoute("Route_Main", listOf(mockRoute, mockRoute, mockRoute))
        verify(mockView).enableActionButton()
    }

    @Test
    fun `when initialize and we receive error while fetching routes from backend then show error content`() {
        whenever(mockView.getReceivedParams()).thenReturn(mockReceivedRide)

        whenever(rideService.findRoutes(any(), any(), any(), any()))
              .thenJust(ResponseResult.Failure(mockError))

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        verify(mockCommonView).showContentError()
    }

    @Test
    fun `when initialize and we receive data from backend then get recommendations on the fly for driver`() {
        whenever(mockView.getReceivedParams()).thenReturn(mockReceivedRide)

        whenever(rideService.findRoutes(any(), any(), any(), any()))
              .thenJust(listOf(mockRoute, mockRoute, mockRoute).toResponseResult())
        whenever(rideService.getRecommendationsOnTheFlyForDriver(any(), any(), any(), any()))
              .thenJust(emptyList<RecommendationOnTheFly>().toResponseResult())

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        verify(rideService).getRecommendationsOnTheFlyForDriver(
              mockReceivedRide.model.destination.location,
              mockReceivedRide.model.origin.location,
              mockReceivedRide.model.route?.polyline,
              mockReceivedRide.model.startTime
        )
    }

    @Test
    fun `when get recommendations on the fly for driver get recommendations then redraw pins for recommendations on the fly`() {
        whenever(mockView.getReceivedParams()).thenReturn(mockReceivedRide)

        whenever(rideService.findRoutes(any(), any(), any(), any()))
              .thenJust(listOf(mockRoute, mockRoute, mockRoute).toResponseResult())
        whenever(rideService.getRecommendationsOnTheFlyForDriver(any(), any(), any(), any()))
              .thenJust(emptyList<RecommendationOnTheFly>().toResponseResult())

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        verify(mockView).redrawPinsForRecommendationsOnTheFly(mockCurrentRecommendations)
    }

    @Test
    fun `when add waypoint button clicked then open address search screen`() {
        whenever(mockView.getReceivedParams()).thenReturn(mockReceivedRide)

        whenever(rideService.findRoutes(any(), any(), any(), any()))
              .thenJust(emptyList<Route>().toResponseResult())

        whenever(heartNavigator.getLauncher<OpenAddressSearch>(any()))
              .thenReturn(activityLauncherOpenTest)
        activityLauncherOpenTest.setActivityResult(Maybe.just(mockLocationFirst))

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actions.onNext(WayPointsActivityPresenter.Action.AddWayPointClicked)

        activityLauncherOpenTest.test()
              .startActivityForResultCalled()
    }

    @Test
    fun `when add waypoint button clicked and we receive an address then draw the pin and fetch new routes`() {
        whenever(mockView.getReceivedParams()).thenReturn(mockReceivedRide)

        whenever(rideService.findRoutes(any(), any(), any(), any()))
              .thenJust(emptyList<Route>().toResponseResult())

        whenever(heartNavigator.getLauncher<OpenAddressSearch>(any()))
              .thenReturn(activityLauncherOpenTest)
        activityLauncherOpenTest.setActivityResult(Maybe.just(mockLocationFirst))

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        reset(mockView)

        actions.onNext(WayPointsActivityPresenter.Action.AddWayPointClicked)

        verify(mockView).addPinToMap("Point_1", mockLocationFirst.location, MarkerType.WAYPOINT_SCREEN_WAYPOINT)

        verify(rideService).findRoutes(
              origin = mockReceivedRide.model.origin.location,
              destination = mockReceivedRide.model.destination.location,
              startTime = mockReceivedRide.model.startTime,
              stops = mockReceivedRide.model.route!!.stops
        )
    }

    @Test
    fun `when clicking add waypoint button for multiple times then increase number of point in the map`() {
        whenever(mockView.getReceivedParams()).thenReturn(mockReceivedRide)

        whenever(rideService.findRoutes(any(), any(), any(), any()))
              .thenJust(emptyList<Route>().toResponseResult())

        whenever(heartNavigator.getLauncher<OpenAddressSearch>(any()))
              .thenReturn(activityLauncherOpenTest)
        activityLauncherOpenTest.setActivityResult(Maybe.just(mockLocationFirst))

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actions.onNext(WayPointsActivityPresenter.Action.AddWayPointClicked)
        actions.onNext(WayPointsActivityPresenter.Action.AddWayPointClicked)

        reset(mockView)

        actions.onNext(WayPointsActivityPresenter.Action.AddWayPointClicked)

        verify(mockView).addPinToMap(
              "Point_3",
              mockLocationFirst.location,
              MarkerType.WAYPOINT_SCREEN_WAYPOINT
        )
    }

    @Test
    fun `when a waypoint in the map is clicked and its location is not exist then dont do anything`() {
        whenever(mockView.getReceivedParams()).thenReturn(mockReceivedRide)

        whenever(rideService.findRoutes(any(), any(), any(), any()))
              .thenJust(emptyList<Route>().toResponseResult())

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        reset(mockView)
        reset(mockCommonView)
        reset(rideService)

        actions.onNext(WayPointsActivityPresenter.Action.WayPointInMapClicked("dummy_key"))

        verifyZeroInteractions(mockView)
        verifyZeroInteractions(mockCommonView)
        verifyZeroInteractions(rideService)
    }

    @Test
    fun `when a waypoint in the map is clicked and its location is exist then show edit wayoint bottom sheet`() {

        addThreeWaypoints()

        reset(mockView)

        actions.onNext(WayPointsActivityPresenter.Action.WayPointInMapClicked("Point_2"))

        verify(mockView).showEditWaypointBottomSheet("Point_2", mockLocationSecond.location)
    }

    @Test
    fun `when delete waypoint button is clicked then hide bottom sheet and remove its pin and request a new route from backend`() {

        addThreeWaypoints()

        reset(mockView)
        clearInvocations(rideService)

        actions.onNext(WayPointsActivityPresenter.Action.WayPointEditSheetDeleteClicked("Point_2"))

        verify(mockView).removePinFromMap("Point_2")

        verify(rideService).findRoutes(
              origin = mockReceivedRide.model.origin.location,
              destination = mockReceivedRide.model.destination.location,
              startTime = mockReceivedRide.model.startTime,
              stops = listOf(mockLocationFirst.location, mockLocationThird.location) + mockReceivedRide.model.route!!.stops
        )
    }

    @Test
    fun `when edit waypoint button is clicked then hide bottom sheet and request for new waypoint address`() {

        addThreeWaypoints()

        reset(mockView)
        clearInvocations(heartNavigator)

        activityLauncherOpenTest.setActivityResult(Maybe.just(mockLocationFirst))

        actions.onNext(WayPointsActivityPresenter.Action.WayPointEditSheetEditClicked("Point_2"))

        activityLauncherOpenTest.test()
              .startActivityForResultCalled()
    }

    @Test
    fun `when edit waypoint button is clicked and we receive a new location then remove previous pin and draw a new pin and request for new routes`() {

        addThreeWaypoints()

        reset(mockView)
        clearInvocations(rideService)

        activityLauncherOpenTest.setActivityResult(Maybe.just(mockLocationForth))

        actions.onNext(WayPointsActivityPresenter.Action.WayPointEditSheetEditClicked("Point_2"))

        verify(mockView).removePinFromMap("Point_2")

        verify(mockView).addPinToMap(
              "Point_4",
              mockLocationForth.location,
              MarkerType.WAYPOINT_SCREEN_WAYPOINT
        )

        verify(rideService).findRoutes(
              origin = mockReceivedRide.model.origin.location,
              destination = mockReceivedRide.model.destination.location,
              startTime = mockReceivedRide.model.startTime,
              stops = listOf(
                    mockLocationFirst.location,
                    mockLocationThird.location,
                    mockLocationForth.location
              ) + mockReceivedRide.model.route!!.stops
        )
    }

    @Test
    fun `when we did not receive any routes from backend and review button is clicked then do nothing`() {
        whenever(mockView.getReceivedParams()).thenReturn(mockReceivedRide.copy(model = mockReceivedRide.model.copy(route = null)))

        whenever(rideService.findRoutes(any(), any(), any(), any()))
              .thenJust(emptyList<Route>().toResponseResult())
        whenever(rideService.getRecommendationsOnTheFlyForDriver(any(), any(), any(), any()))
              .thenJust(emptyList<RecommendationOnTheFly>().toResponseResult())

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actions.onNext(WayPointsActivityPresenter.Action.PreviewClicked)

        verifyZeroInteractions(heartNavigator)
    }

    @Test
    fun `when review button is clicked and we have proper list of routes then redirect to review page by passing ride object`() {
        whenever(mockView.getReceivedParams()).thenReturn(mockReceivedRide)

        whenever(rideService.findRoutes(any(), any(), any(), any()))
              .thenJust(listOf(mockRoute, mockRoute, mockRoute).toResponseResult())

        whenever(rideService.getRecommendationsOnTheFlyForDriver(any(), any(), any(), any()))
              .thenJust(emptyList<RecommendationOnTheFly>().toResponseResult())
        val activityLauncher = ActivityLauncherOpenTest()
        whenever(heartNavigator.getLauncher<OpenCreateReviewScreen>(any()))
              .thenReturn(activityLauncher)

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actions.onNext(WayPointsActivityPresenter.Action.PreviewClicked)

        activityLauncher.test()
              .startActivityCalled()
    }

    private fun addThreeWaypoints() {
        whenever(mockView.getReceivedParams()).thenReturn(mockReceivedRide)

        whenever(rideService.findRoutes(any(), any(), any(), any()))
              .thenJust(emptyList<Route>().toResponseResult())

        whenever(heartNavigator.getLauncher<OpenAddressSearch>(any()))
              .thenReturn(activityLauncherOpenTest)

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        activityLauncherOpenTest.setActivityResult(Maybe.just(mockLocationFirst))
        actions.onNext(WayPointsActivityPresenter.Action.AddWayPointClicked)
        activityLauncherOpenTest.setActivityResult(Maybe.just(mockLocationSecond))
        actions.onNext(WayPointsActivityPresenter.Action.AddWayPointClicked)
        activityLauncherOpenTest.setActivityResult(Maybe.just(mockLocationThird))
        actions.onNext(WayPointsActivityPresenter.Action.AddWayPointClicked)
    }
}
