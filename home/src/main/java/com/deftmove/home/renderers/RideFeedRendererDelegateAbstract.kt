package com.deftmove.home.renderers

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.deftmove.carpooling.commonui.extension.isActive
import com.deftmove.carpooling.interfaces.authentication.model.UserModel
import com.deftmove.carpooling.interfaces.ride.model.RideForFeed
import com.deftmove.carpooling.interfaces.ride.model.RideRole
import com.deftmove.heart.common.extension.dp2px
import com.deftmove.heart.common.extension.toRelativeDateTimeString
import com.deftmove.heart.common.ui.extension.loadAvatarWithBorder
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.heart.interfaces.map.Coordinate
import com.deftmove.heart.maptools.MapMarkerFactory
import com.deftmove.heart.maptools.mapbuilder.MarkerType
import com.deftmove.heart.maptools.toLatLng
import com.deftmove.home.HomePresenter
import com.deftmove.home.R
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewFinder
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import io.reactivex.subjects.Subject

abstract class RideFeedRendererDelegateAbstract {
    open fun bind(
        actions: Subject<PresenterAction>,
        currentUser: UserModel,
        rideForFeed: RideForFeed,
        finder: ViewFinder
    ) {

        prepareMap(actions, finder, currentUser, rideForFeed)
        showDate(finder, rideForFeed)
        showLocations(finder, rideForFeed)
        showRoleTextBottomPart(finder, rideForFeed)
        prepareEditIconClick(actions, rideForFeed, finder)
        prepareRepeating(finder, rideForFeed)
    }

    private fun prepareRepeating(finder: ViewFinder, rideForFeed: RideForFeed) {
        finder.find<View>(R.id.ride_feed_item_repeating_icon).isGone = !rideForFeed.repeat.isActive()
    }

    private fun prepareMap(
        actions: Subject<PresenterAction>,
        finder: ViewFinder,
        currentUser: UserModel,
        rideForFeed: RideForFeed
    ) {
        val bounds = LatLngBounds.Builder()
        bounds.include(rideForFeed.origin.coordinate.toLatLng())
        bounds.include(rideForFeed.destination.coordinate.toLatLng())
        bounds.addAdditionalPadding()

        val mapViewLayout: ViewGroup = finder.find(R.id.ride_feed_item_google_map_view)
        val options = GoogleMapOptions()
              .liteMode(true)
              .compassEnabled(false)
              .rotateGesturesEnabled(false)
              .tiltGesturesEnabled(false)
              .latLngBoundsForCameraTarget(bounds.build())
              .mapType(GoogleMap.MAP_TYPE_NORMAL)
              .mapToolbarEnabled(false)

        val mapView = MapView(mapViewLayout.context, options)
        mapViewLayout.removeAllViews()
        mapViewLayout.addView(mapView)

        mapView.onCreate(null)
        mapView.getMapAsync { googleMap ->
            googleMap.setOnMapClickListener {
                actions.onNext(HomePresenter.Action.RideCardClicked(rideId = rideForFeed.id))
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 0))

            when (rideForFeed.role) {
                RideRole.DRIVER -> {

                    if (!rideForFeed.route?.polyline.isNullOrEmpty()) {
                        googleMap.addPolyline(
                              PolylineOptions()
                                    .addAll(PolyUtil.decode(rideForFeed.route?.polyline))
                                    .color(ContextCompat.getColor(mapView.context, R.color.primary))
                                    .width(dp2px(DRIVER_ROUTE_WIDTH))
                        )
                    }

                    googleMap.addMarker(
                          MapMarkerFactory.makeMarkerOptionsWithType(
                                MarkerType.RIDE_FEED_DRIVER_ORIGIN,
                                rideForFeed.origin
                          )
                    )
                    googleMap.addMarker(
                          MapMarkerFactory.makeMarkerOptionsWithType(
                                MarkerType.RIDE_FEED_DRIVER_DESTINATION,
                                rideForFeed.destination
                          )
                    )
                }

                RideRole.PASSENGER -> {

                    googleMap.addMarker(
                          MapMarkerFactory.makeMarkerOptionsWithType(
                                MarkerType.RIDE_FEED_PASSENGER_DESTINATION,
                                rideForFeed.destination
                          )
                    )

                    mapView.context.loadAvatarWithBorder(currentUser.avatarUrl) { bitmap ->
                        Handler(Looper.getMainLooper()).post {
                            googleMap.addMarker(
                                  MapMarkerFactory.makeMarkerOptionsWithType(
                                        MarkerType.RIDE_FEED_PASSENGER_ORIGIN(bitmap),
                                        rideForFeed.origin
                                  )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun prepareEditIconClick(
        actions: Subject<PresenterAction>,
        rideForFeed: RideForFeed,
        finder: ViewFinder
    ) {
        val imgEditIcon: ImageView = finder.find(R.id.ride_feed_item_edit_bottom)
        imgEditIcon.setOnClickListener { view ->
            PopupMenu(view.context, view).apply {
                setOnMenuItemClickListener { menuItem ->
                    return@setOnMenuItemClickListener when (menuItem.itemId) {
                        R.id.ride_renderer_action_edit_ride_routine -> {
                            actions.onNext(HomePresenter.Action.MenuEditClicked(rideForFeed, updateRepeatingRides = true))
                            true
                        }

                        R.id.ride_renderer_action_edit,
                        R.id.ride_renderer_action_edit_current_ride -> {
                            actions.onNext(HomePresenter.Action.MenuEditClicked(rideForFeed, updateRepeatingRides = false))
                            true
                        }

                        R.id.ride_renderer_action_cancel -> {
                            actions.onNext(HomePresenter.Action.MenuCancelClicked(rideForFeed.id))
                            true
                        }

                        R.id.ride_renderer_action_cancel_all -> {
                            actions.onNext(HomePresenter.Action.MenuCancelAllRepeatingClicked(rideForFeed.id))
                            true
                        }

                        else -> false
                    }
                }
                inflate(if (rideForFeed.repeat.isActive()) R.menu.menu_repeati_ride_renderer else R.menu.menu_ride_renderer)
                show()
            }
        }
    }

    private fun showDate(
        finder: ViewFinder,
        rideForFeed: RideForFeed
    ) {
        val txtDate: TextView = finder.find(R.id.ride_feed_item_date)
        txtDate.text = rideForFeed.time.toRelativeDateTimeString(txtDate.context)
    }

    private fun showLocations(
        finder: ViewFinder,
        rideForFeed: RideForFeed
    ) {
        val txtOrigin: TextView = finder.find(R.id.ride_feed_item_route_origin)
        val txtDestination: TextView = finder.find(R.id.ride_feed_item_route_destination)
        txtOrigin.text = rideForFeed.origin.address
        txtDestination.text = rideForFeed.destination.address
    }

    private fun showRoleTextBottomPart(
        finder: ViewFinder,
        rideForFeed: RideForFeed
    ) {
        val txtRoleText: TextView = finder.find(R.id.ride_feed_item_role_bottom)
        when (rideForFeed.role) {
            RideRole.DRIVER -> txtRoleText.setText(R.string.activity_ride_feed_item_role_text_driver)
            RideRole.PASSENGER -> txtRoleText.setText(
                  R.string.activity_ride_feed_item_role_text_passenger
            )
        }
    }

    private fun LatLngBounds.Builder.addAdditionalPadding(): LatLngBounds.Builder {
        val northeast = this.build().northeast
        val southwest = this.build().southwest

        val additionalPaddingLatitude: Double = Math.abs(northeast.latitude - southwest.latitude) / 2
        val additionalPaddingLongitude: Double = Math.abs(northeast.longitude - southwest.longitude) / 2

        val northeastCoordinate = Coordinate(
              latitude = northeast.latitude + additionalPaddingLatitude,
              longitude = northeast.longitude + additionalPaddingLongitude
        )
        val southwestCoordinate = Coordinate(
              latitude = southwest.latitude - additionalPaddingLatitude,
              longitude = southwest.longitude - additionalPaddingLongitude
        )

        this.include(northeastCoordinate.toLatLng())
        this.include(southwestCoordinate.toLatLng())

        return this
    }

    companion object {
        private const val DRIVER_ROUTE_WIDTH: Int = 5
    }
}
