package com.deftmove.ride.review

import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.widget.NestedScrollView
import com.deftmove.carpooling.commonui.extension.isActive
import com.deftmove.carpooling.commonui.extension.toText
import com.deftmove.carpooling.interfaces.ride.create.model.CreateRideReviewModel
import com.deftmove.carpooling.interfaces.ride.create.model.RepeatingRideModel
import com.deftmove.heart.common.extension.dp2px
import com.deftmove.heart.common.extension.toRelativeDateTimeString
import com.deftmove.heart.common.ui.ActivityWithPresenter
import com.deftmove.heart.common.ui.ScreenBucket
import com.deftmove.heart.common.ui.extension.bindView
import com.deftmove.heart.interfaces.common.ui.ScreenBucketModel
import com.deftmove.heart.interfaces.map.Location
import com.deftmove.heart.interfaces.model.SearchAddressPrediction
import com.deftmove.heart.interfaces.navigator.retrieveArgument
import com.deftmove.heart.maptools.mapbuilder.MapProvider
import com.deftmove.heart.maptools.mapbuilder.MarkerType
import com.deftmove.ride.R
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import org.koin.androidx.scope.currentScope
import timber.log.Timber
import java.util.Date

class CreateRideReviewActivity : ActivityWithPresenter() {
    override fun getCurrentScreenBucket(): ScreenBucket<*> {
        return object : ScreenBucket<CreateRideReviewPresenter.View>(
              ScreenBucketModel(
                    scope = currentScope,
                    viewLayout = R.layout.activity_create_ride_review,
                    enableDisplayHomeAsUp = true
              )
        ) {
            override fun getPresenterView(): CreateRideReviewPresenter.View {
                return object : CreateRideReviewPresenter.View {
                    override fun getReceivedParams(): CreateRideReviewModel {
                        return currentCreateRideModel!!
                    }

                    override fun showStartTime(startTime: Date) {
                        runOnUiThread {
                            txtDate.text = startTime.toRelativeDateTimeString(baseContext)
                        }
                    }

                    override fun showCarDetails(carModel: String?, carLicensePlate: String?) {
                        runOnUiThread {
                            txtCarModel.text = carModel
                            txtCarLicenseNumber.text = carLicensePlate
                        }
                    }

                    override fun showRepeating(repeatingRideModel: RepeatingRideModel) {
                        repeatingText.isGone = !repeatingRideModel.isActive()
                        if (repeatingRideModel.isActive()) {
                            repeatingText.text =
                                  repeatingRideModel.toText(textUtils, R.string.activity_create_ride_review_days_every)
                        }
                    }

                    override fun drawRoute(polygon: String) {
                        runOnUiThread {
                            mapProvider.addRoute(
                                  key = "review_screen_main_route",
                                  polylineOptions = PolylineOptions()
                                        .addAll(PolyUtil.decode(polygon))
                                        .color(ContextCompat.getColor(baseContext, R.color.grey))
                                        .width(dp2px(REVIEW_ROUTE_WIDTH))
                            )
                        }
                    }

                    override fun drawOriginPin(origin: Location) {
                        runOnUiThread {
                            mapProvider.addMarker(
                                  key = "review_screen_origin",
                                  location = origin,
                                  markerType = MarkerType.DRIVER_REVIEW_SCREEN_ORIGIN
                            )
                        }
                    }

                    override fun drawDestinationPin(destination: Location) {
                        runOnUiThread {
                            mapProvider.addMarker(
                                  key = "review_screen_destination",
                                  location = destination,
                                  markerType = MarkerType.DRIVER_REVIEW_SCREEN_DESTINATION
                            )
                        }
                    }

                    override fun drawWaypointPins(waypoints: List<Location>) {
                        runOnUiThread {
                            waypoints.forEachIndexed { index, location ->
                                mapProvider.addMarker(
                                      key = "review_screen_waypoint_$index",
                                      location = location,
                                      markerType = MarkerType.DRIVER_REVIEW_SCREEN_WAYPOINT
                                )
                            }
                        }
                    }

                    override fun showOriginAddress(origin: SearchAddressPrediction, time: Date) {
                        runOnUiThread {
                            routeOrigin.bind(origin, time)
                        }
                    }

                    override fun showDestinationAddress(destination: SearchAddressPrediction, time: Date) {
                        runOnUiThread {
                            routeDestination.bind(destination, time)
                        }
                    }

                    override fun showPickupPointAddresses(stops: List<Pair<Location, Date>>) {
                        runOnUiThread {
                            stops.forEach { (prediction, time) -> addPickupPointToThePath(prediction, time) }
                        }
                    }

                    override fun showSaveButton() {
                        submitButton.setText(R.string.activity_create_ride_review_save_button)
                    }

                    override fun disableSubmitButton() {
                        submitButton.setText(R.string.activity_create_ride_review_save_button)
                    }

                    override fun showMustUpdateCarDetailTitle() {
                        txtCarDetailTitle.text = getString(R.string.activity_create_ride_review_must_update_car_detail)
                    }
                }
            }
        }
    }

    private fun addPickupPointToThePath(location: Location, time: Date) {
        val view = ReviewPickupItemView(this)
        view.bind(location, time)

        routePathView.addPickupPoint(view)
        routes.addView(view)
    }

    private val scrollViewRoot: NestedScrollView by bindView(R.id.create_ride_review_scrollview_root)
    private val txtDate: TextView by bindView(R.id.create_ride_review_date_text)
    private val routePathView: com.deftmove.carpooling.commonui.ui.RoutePathView by bindView(R.id.create_ride_review_route_path)
    private val routeOrigin: ReviewPickupItemView by bindView(R.id.create_ride_review_route_origin)
    private val routeDestination: ReviewPickupItemView by bindView(R.id.create_ride_review_route_destination)
    private val txtCarDetailTitle: TextView by bindView(R.id.create_ride_review_car_details_title_text)
    private val routes: LinearLayout by bindView(R.id.create_ride_review_routes)
    private val txtCarModel: TextView by bindView(R.id.create_ride_review_car_model_text)
    private val txtCarLicenseNumber: TextView by bindView(R.id.create_ride_review_car_number_text)
    private val repeatingText: TextView by bindView(R.id.create_ride_review_repeating_text)
    private val submitButton: Button by bindView(R.id.create_ride_review_submit_button)

    private var currentCreateRideModel: CreateRideReviewModel? = null
    private lateinit var mapProvider: MapProvider

    override fun initializeBeforePresenter() {

        currentCreateRideModel = retrieveArgument()
        Timber.d("currentCreateRideModel:${currentCreateRideModel.toString()}")
    }

    override fun initializeViewListeners() {

        enableSmartElevationForActionBar(scrollViewRoot)

        mapProvider = MapProvider.builder()
              .makeEverythingVisible()
              .addPadding()
              .animateCamera()
              .setDefaultLocationCurrentLocation(DEFAULT_ZOOM_FOR_CURRENT_LOCATION)
              .disposeBy(disposables)
              .build(supportFragmentManager)

        mapProvider.startWith(R.id.create_ride_review_google_map_fragment)

        submitButton.setOnClickListener {
            actions.onNext(CreateRideReviewPresenter.Action.SubmitClicked)
        }
    }

    companion object {
        private const val DEFAULT_ZOOM_FOR_CURRENT_LOCATION: Float = 10.0f
        private const val REVIEW_ROUTE_WIDTH: Int = 5
    }
}
