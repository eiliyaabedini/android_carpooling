package com.deftmove.ride.waypoints

import com.deftmove.carpooling.interfaces.OpenAddressSearch
import com.deftmove.carpooling.interfaces.OpenCreateReviewScreen
import com.deftmove.carpooling.interfaces.OpenProfilePublicScreen
import com.deftmove.carpooling.interfaces.ride.create.model.CreateRideAddWayPointModel
import com.deftmove.carpooling.interfaces.ride.create.model.CreateRideModel
import com.deftmove.carpooling.interfaces.ride.model.RecommendationOnTheFly
import com.deftmove.carpooling.interfaces.ride.service.RideService
import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.TextUtils
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonAction
import com.deftmove.heart.interfaces.common.presenter.PresenterView
import com.deftmove.heart.interfaces.common.rx.doOnData
import com.deftmove.heart.interfaces.map.Location
import com.deftmove.heart.interfaces.map.Route
import com.deftmove.heart.interfaces.map.service.LocationFulfiller
import com.deftmove.heart.interfaces.model.SearchAddressPrediction
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import com.deftmove.heart.maptools.mapbuilder.MarkerType
import com.deftmove.resources.R
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

class WayPointsActivityPresenter(
    private val rideService: RideService,
    private val locationFulfiller: LocationFulfiller,
    private val textUtils: TextUtils,
    private val heartNavigator: HeartNavigator,
    private val reactiveTransformer: ReactiveTransformer
) : Presenter<WayPointsActivityPresenter.View>() {

    private val updateRouteSubject: BehaviorSubject<CreateRideModel> = BehaviorSubject.create()
    private val fetchRecommendationsSubject: BehaviorSubject<CreateRideModel> = BehaviorSubject.create()

    private lateinit var currentCreateRideModel: CreateRideModel
    private var addedStops: LinkedHashMap<String, Location> = linkedMapOf()
    private var addedRecommendations: LinkedHashMap<String, RecommendationOnTheFly> = linkedMapOf()
    private var currentRecommendations: LinkedHashMap<String, RecommendationOnTheFly> = linkedMapOf()
    private val lastAddedWayPointNumber: AtomicInteger = AtomicInteger()

    override fun initialise() {
        currentCreateRideModel = view?.getReceivedParams()!!.model

        updateRouteSubject
              .subscribeOn(reactiveTransformer.ioScheduler())
              .switchMapSingle { createRideModel ->

                  commonView?.showContentLoading()
                  view?.disableActionButton()

                  rideService.findRoutes(
                        destination = createRideModel.destination.location,
                        origin = createRideModel.origin.location,
                        startTime = createRideModel.startTime,
                        stops = addedStops.values.toList() +
                              addedRecommendations.values.map { it.location } +
                              (currentCreateRideModel.route?.stops ?: emptyList())
                  )
              }
              .doOnData { routes ->
                  //If we don't receive any routes from backend it means that something failed in backend side
                  if (routes.isNotEmpty()) {
                      currentCreateRideModel.route = routes.first()

                      view?.drawRoute("Route_Main", routes)

                      view?.enableActionButton()

                      fetchRecommendationsOnTheFly()
                  }

                  commonView?.showContent()
              }
              .subscribeSafeResponseWithShowingErrorContent()

        fetchRecommendationsSubject
              .subscribeOn(reactiveTransformer.ioScheduler())
              .switchMapSingle { createRideModel ->

                  rideService.getRecommendationsOnTheFlyForDriver(
                        destination = createRideModel.destination.location,
                        origin = createRideModel.origin.location,
                        polyline = createRideModel.route?.polyline,
                        startTime = createRideModel.startTime
                  )
              }
              .doOnData { recommendationsOnTheFly ->
                  updateRecommendationsInMap(recommendationsOnTheFly)
              }
              .subscribeSafeResponseWithShowingErrorContent()

        commonView?.actions()
              ?.subscribeOn(reactiveTransformer.ioScheduler())
              ?.subscribeSafeWithShowingErrorContent { action ->
                  Timber.d("action:%s", action)
                  when (action) {
                      is PresenterCommonAction.ErrorRetryClicked -> {
                          commonView?.showContent()
                      }

                      is Action.AddWayPointClicked -> {
                          requestForNewWayPoint()
                      }

                      is Action.WayPointInMapClicked -> {

                          when {
                              addedStops.containsKey(action.waypointKey) -> {
                                  val location: Location? = addedStops[action.waypointKey]

                                  location?.let {
                                      view?.showEditWaypointBottomSheet(action.waypointKey, it)
                                  }
                              }

                              addedRecommendations.containsKey(action.waypointKey) -> {
                                  val recommendationOnTheFly: RecommendationOnTheFly? =
                                        addedRecommendations[action.waypointKey]

                                  recommendationOnTheFly?.let {
                                      view?.showEditWaypointBottomSheetAddedRecommendation(
                                            action.waypointKey,
                                            locationFulfiller.fulfill(it.location),
                                            it.firstName
                                      )
                                  }
                              }

                              currentRecommendations.containsKey(action.waypointKey) -> {
                                  val recommendationOnTheFly: RecommendationOnTheFly? =
                                        currentRecommendations[action.waypointKey]

                                  recommendationOnTheFly?.let {
                                      view?.showEditWaypointBottomSheetRecommendation(
                                            action.waypointKey,
                                            locationFulfiller.fulfill(it.location),
                                            recommendationOnTheFly.firstName
                                      )
                                  }
                              }
                          }
                      }

                      is Action.WayPointEditSheetDeleteClicked -> {
                          addedStops.remove(action.waypointKey)
                          view?.removePinFromMap(action.waypointKey)

                          fetchNewRoute()
                      }

                      is Action.WayPointEditSheetEditClicked -> {
                          val location: Location? = addedStops[action.waypointKey]

                          location?.let {
                              requestForNewWayPoint(action.waypointKey)
                          }
                      }

                      is Action.WayPointRecommendedSheetDeleteClicked -> {
                          addedRecommendations.remove(action.waypointKey)
                          view?.removePinFromMap(action.waypointKey)

                          fetchNewRoute()
                      }

                      is Action.WayPointRecommendedSheetAddClicked -> {
                          val recommendationOnTheFly: RecommendationOnTheFly? = currentRecommendations[action.waypointKey]

                          recommendationOnTheFly?.let {
                              val key = makeRecommendationKey(recommendationOnTheFly)
                              addedRecommendations[key] = recommendationOnTheFly

                              view?.addRecommendedWaypointToMap(
                                    key, recommendationOnTheFly
                              )
                          }

                          updateRecommendationsInMap()

                          fetchNewRoute()
                      }

                      is Action.WayPointRecommendedSheetAvatarClicked -> {
                          val recommendationOnTheFly: RecommendationOnTheFly? = currentRecommendations[action.waypointKey]

                          recommendationOnTheFly?.let {
                              heartNavigator.getLauncher(
                                    OpenProfilePublicScreen(it.userId)
                              )?.startActivity()
                          }
                      }

                      is Action.WayPointAddedRecommendedSheetAvatarClicked -> {
                          val recommendationOnTheFly: RecommendationOnTheFly? = addedRecommendations[action.waypointKey]

                          recommendationOnTheFly?.let {
                              heartNavigator.getLauncher(
                                    OpenProfilePublicScreen(it.userId)
                              )?.startActivity()
                          }
                      }

                      is Action.PreviewClicked -> {
                          currentCreateRideModel.route?.let {
                              heartNavigator.getLauncher(
                                    OpenCreateReviewScreen(currentCreateRideModel)
                              )?.startActivity()
                          }
                      }
                  }
              }


        initialForCreateOrEdit()
    }

    private fun initialForCreateOrEdit() {
        if (currentCreateRideModel.isEditing && !currentCreateRideModel.route?.stops.isNullOrEmpty()) {
            currentCreateRideModel.route?.stops?.forEach { stopLocation ->
                val newWayPointNumber = lastAddedWayPointNumber.incrementAndGet()
                val key = "Point_$newWayPointNumber"

                view?.addPinToMap(
                      key,
                      stopLocation,
                      MarkerType.WAYPOINT_SCREEN_WAYPOINT
                )

                addedStops[key] = stopLocation
            }
        }

        view?.addPinToMap(
              "way_point_screen_origin",
              currentCreateRideModel.origin.location,
              MarkerType.WAYPOINT_SCREEN_ORIGIN
        )

        view?.addPinToMap(
              "way_point_screen_destination",
              currentCreateRideModel.destination.location,
              MarkerType.WAYPOINT_SCREEN_DESTINATION
        )

        fetchNewRoute()
    }

    private fun updateRecommendationsInMap(recommendationsOnTheFly: List<RecommendationOnTheFly> = currentRecommendations.values.toList()) {
        currentRecommendations.keys.forEach { key ->
            view?.removePinFromMap(key)
        }

        val filteredRecommendations = recommendationsOnTheFly.filter { recommendationOnTheFly ->
            !addedRecommendations.containsKey(makeRecommendationKey(recommendationOnTheFly))
        }

        currentRecommendations.clear()
        filteredRecommendations.forEach { recommendationOnTheFly ->

            currentRecommendations[makeRecommendationKey(recommendationOnTheFly)] = recommendationOnTheFly
        }

        view?.redrawPinsForRecommendationsOnTheFly(currentRecommendations)
    }

    private fun makeRecommendationKey(recommendationOnTheFly: RecommendationOnTheFly): String {
        return "Recommendation_" +
              "${recommendationOnTheFly.userId}_" +
              "${recommendationOnTheFly.location.coordinate.latitude}_" +
              "${recommendationOnTheFly.location.coordinate.longitude}"
    }

    private fun requestForNewWayPoint(locationKey: String? = null) {
        heartNavigator.getLauncher(
              OpenAddressSearch(
                    defaultLocation = if (locationKey != null) addedStops[locationKey] else null,
                    addressTypeName = textUtils.getString(R.string.common_waypoint)
              )
        )
              ?.startActivityForResult()
              ?.map { it as SearchAddressPrediction }
              ?.doOnSuccess { prediction ->
                  val newWayPointNumber = lastAddedWayPointNumber.incrementAndGet()
                  val key = "Point_$newWayPointNumber"

                  locationKey?.let {
                      addedStops.remove(it)
                      view?.removePinFromMap(it)
                  }

                  view?.addPinToMap(
                        key,
                        prediction.location,
                        MarkerType.WAYPOINT_SCREEN_WAYPOINT
                  )

                  addedStops[key] = prediction.location

                  fetchNewRoute()
              }?.subscribeSafeWithShowingErrorContent()
    }

    private fun fetchNewRoute() {
        updateRouteSubject.onNext(currentCreateRideModel)
    }

    private fun fetchRecommendationsOnTheFly() {
        fetchRecommendationsSubject.onNext(currentCreateRideModel)
    }

    interface View : PresenterView {
        fun getReceivedParams(): CreateRideAddWayPointModel

        fun addPinToMap(key: String, location: Location, markerType: MarkerType)
        fun addRecommendedWaypointToMap(key: String, recommendationOnTheFly: RecommendationOnTheFly)
        fun redrawPinsForRecommendationsOnTheFly(recommendationsOnTheFly: Map<String, RecommendationOnTheFly>)
        fun removePinFromMap(key: String)
        fun drawRoute(key: String, routes: List<Route>)

        fun showEditWaypointBottomSheet(key: String, location: Location)
        fun showEditWaypointBottomSheetAddedRecommendation(key: String, location: Location, firsName: String)
        fun showEditWaypointBottomSheetRecommendation(key: String, location: Location, firsName: String)

        fun disableActionButton()
        fun enableActionButton()
    }

    sealed class Action : PresenterAction {
        object AddWayPointClicked : Action()

        data class WayPointInMapClicked(val waypointKey: String) : Action()
        data class WayPointEditSheetDeleteClicked(val waypointKey: String) : Action()
        data class WayPointEditSheetEditClicked(val waypointKey: String) : Action()
        data class WayPointRecommendedSheetDeleteClicked(val waypointKey: String) : Action()
        data class WayPointRecommendedSheetAddClicked(val waypointKey: String) : Action()
        data class WayPointRecommendedSheetAvatarClicked(val waypointKey: String) : Action()
        data class WayPointAddedRecommendedSheetAvatarClicked(val waypointKey: String) : Action()

        object PreviewClicked : Action()
    }
}
