package com.deftmove.ride.waypoints

import android.view.View
import androidx.core.content.ContextCompat
import com.deftmove.carpooling.interfaces.ride.create.model.CreateRideAddWayPointModel
import com.deftmove.carpooling.interfaces.ride.model.RecommendationOnTheFly
import com.deftmove.heart.common.extension.dp2px
import com.deftmove.heart.common.ui.ActivityWithPresenter
import com.deftmove.heart.common.ui.ScreenBucket
import com.deftmove.heart.common.ui.extension.bindView
import com.deftmove.heart.common.ui.extension.loadAvatarWithBorder
import com.deftmove.heart.common.ui.ui.bottomsheet.TextBottomSheet
import com.deftmove.heart.common.ui.ui.bottomsheet.model.TextBottomSheetModel
import com.deftmove.heart.interfaces.common.ui.ScreenBucketModel
import com.deftmove.heart.interfaces.map.Location
import com.deftmove.heart.interfaces.map.Route
import com.deftmove.heart.interfaces.navigator.retrieveArgument
import com.deftmove.heart.maptools.mapbuilder.MapProvider
import com.deftmove.heart.maptools.mapbuilder.MarkerType
import com.deftmove.ride.R
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import org.koin.androidx.scope.currentScope
import timber.log.Timber

class WayPointsActivity : ActivityWithPresenter() {

    override fun getCurrentScreenBucket(): ScreenBucket<*> {
        return object : ScreenBucket<WayPointsActivityPresenter.View>(
              ScreenBucketModel(
                    scope = currentScope,
                    viewLayout = R.layout.activity_way_points,
                    enableDisplayHomeAsUp = true
              )
        ) {
            override fun getPresenterView(): WayPointsActivityPresenter.View {
                return object : WayPointsActivityPresenter.View {
                    override fun getReceivedParams(): CreateRideAddWayPointModel {
                        return currentCreateRideModel!!
                    }

                    override fun addPinToMap(key: String, location: Location, markerType: MarkerType) {
                        runOnUiThread {
                            mapProvider.addMarker(key, location, markerType)
                        }
                    }

                    override fun addRecommendedWaypointToMap(key: String, recommendationOnTheFly: RecommendationOnTheFly) {
                        loadAvatarWithBorder(recommendationOnTheFly.userAvatar) { bitmap ->
                            runOnUiThread {
                                mapProvider.addMarker(
                                      key,
                                      recommendationOnTheFly.location,
                                      MarkerType.WAYPOINT_SCREEN_RECOMMENDATION_ON_THE_FLY(
                                            drawableUtils.mergeDrawableWithBitmap(
                                                  bitmap,
                                                  R.drawable.check_success_with_circle_border,
                                                  true
                                            )
                                      )
                                )
                            }
                        }
                    }

                    override fun redrawPinsForRecommendationsOnTheFly(recommendationsOnTheFly: Map<String, RecommendationOnTheFly>) {
                        recommendationsOnTheFly.forEach { (key, recommendation) ->
                            loadAvatarWithBorder(recommendation.userAvatar) { bitmap ->
                                runOnUiThread {
                                    mapProvider.addMarker(
                                          key,
                                          recommendation.location,
                                          MarkerType.WAYPOINT_SCREEN_RECOMMENDATION_ON_THE_FLY(bitmap)
                                    )
                                }
                            }
                        }

                    }

                    override fun removePinFromMap(key: String) {
                        runOnUiThread {
                            mapProvider.removeMarker(key)
                        }
                    }

                    override fun drawRoute(key: String, routes: List<Route>) {
                        runOnUiThread {
                            mapProvider.addRoute(
                                  key = "way_point_screen_route",
                                  polylineOptions = PolylineOptions()
                                        .addAll(PolyUtil.decode(routes[0].polyline))
                                        .color(ContextCompat.getColor(baseContext, R.color.grey))
                                        .width(dp2px(ROUTE_WIDTH))
                            )
                        }
                    }

                    override fun showEditWaypointBottomSheet(key: String, location: Location) {
                        runOnUiThread {

                            TextBottomSheet.showTexts(
                                  supportFragmentManager = supportFragmentManager,
                                  texts = listOf(
                                        TextBottomSheetModel(
                                              text = textUtils.getString(R.string.way_points_screen_waypoint_edit),
                                              icon = R.drawable.ic_edit_black_24dp,
                                              clickCallback = { delegate ->
                                                  delegate.dismiss()
                                                  actions.onNext(
                                                        WayPointsActivityPresenter.Action.WayPointEditSheetEditClicked(
                                                              key
                                                        )
                                                  )
                                              }
                                        ),
                                        TextBottomSheetModel(
                                              text = textUtils.getString(R.string.way_points_screen_waypoint_delete),
                                              textStyle = R.style.AppTextAppearance_Body_Error,
                                              icon = R.drawable.ic_delete_black_24dp,
                                              clickCallback = { delegate ->
                                                  delegate.dismiss()
                                                  actions.onNext(
                                                        WayPointsActivityPresenter.Action.WayPointEditSheetDeleteClicked(
                                                              key
                                                        )
                                                  )
                                              }
                                        )
                                  ),
                                  title = location.name,
                                  description = location.address
                            )
                        }
                    }

                    override fun showEditWaypointBottomSheetAddedRecommendation(
                        key: String,
                        location: Location,
                        firsName: String
                    ) {
                        TextBottomSheet.showTexts(
                              supportFragmentManager = supportFragmentManager,
                              texts = listOf(
                                    TextBottomSheetModel(
                                          text = textUtils.getString(R.string.way_points_screen_recommendation_visit_profile),
                                          clickCallback = { delegate ->
                                              delegate.dismiss()
                                              actions.onNext(
                                                    WayPointsActivityPresenter.Action.WayPointAddedRecommendedSheetAvatarClicked(
                                                          key
                                                    )
                                              )
                                          }
                                    ),
                                    TextBottomSheetModel(
                                          text = textUtils.getString(R.string.way_points_screen_added_recommendation_delete),
                                          textStyle = R.style.AppTextAppearance_Body_Error,
                                          icon = R.drawable.ic_delete_black_24dp,
                                          clickCallback = { delegate ->
                                              delegate.dismiss()
                                              actions.onNext(
                                                    WayPointsActivityPresenter.Action.WayPointRecommendedSheetDeleteClicked(
                                                          key
                                                    )
                                              )
                                          }
                                    )
                              ),
                              title = firsName,
                              description = location.address
                        )
                    }

                    override fun showEditWaypointBottomSheetRecommendation(
                        key: String,
                        location: Location,
                        firsName: String
                    ) {
                        runOnUiThread {
                            TextBottomSheet.showTexts(
                                  supportFragmentManager = supportFragmentManager,
                                  texts = listOf(
                                        TextBottomSheetModel(
                                              text = textUtils.getString(R.string.way_points_screen_recommendation_add),
                                              clickCallback = { delegate ->
                                                  delegate.dismiss()
                                                  actions.onNext(
                                                        WayPointsActivityPresenter.Action.WayPointRecommendedSheetAddClicked(
                                                              key
                                                        )
                                                  )
                                              }
                                        ),
                                        TextBottomSheetModel(
                                              text = textUtils.getString(R.string.way_points_screen_recommendation_visit_profile),
                                              clickCallback = { delegate ->
                                                  delegate.dismiss()
                                                  actions.onNext(
                                                        WayPointsActivityPresenter.Action.WayPointRecommendedSheetAvatarClicked(
                                                              key
                                                        )
                                                  )
                                              }
                                        )
                                  ),
                                  title = firsName,
                                  description = location.address
                            )
                        }
                    }

                    override fun disableActionButton() {
                        runOnUiThread {
                            btnPreview.isEnabled = false
                        }
                    }

                    override fun enableActionButton() {
                        runOnUiThread {
                            btnPreview.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    private val txtAddWayPoints: View by bindView(R.id.waypoints_bottom_waypoints_action)
    private val btnPreview: View by bindView(R.id.waypoints_bottom_waypoints_review_button)

    private var currentCreateRideModel: CreateRideAddWayPointModel? = null
    private lateinit var mapProvider: MapProvider

    override fun initializeBeforePresenter() {

        currentCreateRideModel = retrieveArgument()
        Timber.d("currentCreateRideModel:${currentCreateRideModel.toString()}")
    }

    override fun initializeViewListeners() {
        txtAddWayPoints.setOnClickListener {
            actions.onNext(WayPointsActivityPresenter.Action.AddWayPointClicked)
        }

        btnPreview.setOnClickListener {
            actions.onNext(WayPointsActivityPresenter.Action.PreviewClicked)
        }

        mapProvider = MapProvider.builder()
              .makeEverythingVisible()
              .addPadding()
              .animateCamera()
              .setDefaultLocationCurrentLocation(DEFAULT_ZOOM_FOR_CURRENT_LOCATION)
              .disposeBy(disposables)
              .build(supportFragmentManager)

        mapProvider.startWith(R.id.waypoints_google_map_fragment)

        mapProvider.observeMarkerClick()
              .doOnNext {
                  actions.onNext(
                        WayPointsActivityPresenter.Action.WayPointInMapClicked(it)
                  )
              }
              .subscribe()
    }

    companion object {
        private const val DEFAULT_ZOOM_FOR_CURRENT_LOCATION: Float = 10.0f
        private const val ROUTE_WIDTH: Int = 5
    }
}
