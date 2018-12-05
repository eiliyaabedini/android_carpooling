package com.deftmove.ride.details

import android.graphics.Point
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.deftmove.carpooling.interfaces.authentication.model.UserModel
import com.deftmove.carpooling.interfaces.ride.details.model.RideDetailsActivityModel
import com.deftmove.heart.common.extension.dp2px
import com.deftmove.heart.common.ui.ActivityWithPresenter
import com.deftmove.heart.common.ui.ScreenBucket
import com.deftmove.heart.common.ui.extension.bindView
import com.deftmove.heart.common.ui.extension.loadAvatarWithBorder
import com.deftmove.heart.common.ui.extension.setMarginBottom
import com.deftmove.heart.common.ui.ui.BottomSheetBehaviorChangeListener
import com.deftmove.heart.common.ui.ui.BottomSheetBehaviorWithHalfExpansion
import com.deftmove.heart.interfaces.common.ui.ScreenBucketModel
import com.deftmove.heart.interfaces.map.Location
import com.deftmove.heart.interfaces.navigator.retrieveArgument
import com.deftmove.heart.maptools.mapbuilder.MapProvider
import com.deftmove.heart.maptools.mapbuilder.MarkerType
import com.deftmove.ride.details.model.ConfirmedPassengerMapMarkerModel
import com.deftmove.ride.details.model.RideDetailsViewModel
import com.deftmove.ride.details.renderer.RideDetailsDriverConfirmedRenderer
import com.deftmove.ride.details.renderer.RideDetailsDriverEmptyRenderer
import com.deftmove.ride.details.renderer.RideDetailsDriverOfferedRenderer
import com.deftmove.ride.details.renderer.RideDetailsDriverRecommendedRenderer
import com.deftmove.ride.details.renderer.RideDetailsDriverRequestedRenderer
import com.deftmove.ride.details.renderer.RideDetailsPassengerConfirmedRenderer
import com.deftmove.ride.details.renderer.RideDetailsPassengerEmptyRenderer
import com.deftmove.ride.details.renderer.RideDetailsPassengerOfferedRenderer
import com.deftmove.ride.details.renderer.RideDetailsPassengerRecommendedRenderer
import com.deftmove.ride.details.renderer.RideDetailsPassengerRequestedRenderer
import com.github.vivchar.rendererrecyclerviewadapter.RendererRecyclerViewAdapter
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewBinder
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.PolyUtil
import org.koin.androidx.scope.currentScope
import timber.log.Timber

class RideDetailsActivity : ActivityWithPresenter() {

    override fun getCurrentScreenBucket(): ScreenBucket<*> {
        return object : ScreenBucket<RideDetailsPresenter.View>(
              ScreenBucketModel(
                    scope = currentScope,
                    viewLayout = R.layout.activity_ride_details,
                    enableDisplayHomeAsUp = false
              )
        ) {
            override fun getPresenterView(): RideDetailsPresenter.View {
                return object : RideDetailsPresenter.View {
                    override fun getReceivedParams(): RideDetailsActivityModel {
                        return currentRideDetailsActivityModel!!
                    }

                    override fun drawCurrentDriverPinAndRouteInMap(
                        origin: Location,
                        destination: Location,
                        polyline: String?
                    ) {
                        runOnUiThread {
                            mapProvider.addMarker(
                                  key = "drawCurrentDriverPinAndRouteInMap_origin",
                                  location = origin,
                                  markerType = MarkerType.RIDE_DETAILS_DRIVER_ORIGIN
                            )

                            mapProvider.addMarker(
                                  key = "drawCurrentDriverPinAndRouteInMap_destination",
                                  location = destination,
                                  markerType = MarkerType.RIDE_DETAILS_DESTINATION
                            )

                            if (!polyline.isNullOrEmpty()) {
                                mapProvider.addRoute(
                                      key = "drawCurrentDriverPinAndRouteInMap_polyline",
                                      polylineOptions = PolylineOptions()
                                            .addAll(PolyUtil.decode(polyline))
                                            .color(ContextCompat.getColor(baseContext, R.color.grey))
                                            .width(dp2px(ROUTE_WIDTH))
                                )
                            }
                        }
                    }

                    override fun drawCurrentPassengerPinAndRouteInMap(
                        currentUser: UserModel,
                        origin: Location,
                        destination: Location,
                        polyline: String?
                    ) {
                        baseContext.loadAvatarWithBorder(currentUser.avatarUrl) { bitmap ->
                            runOnUiThread {
                                mapProvider.addMarker(
                                      key = "drawCurrentPassengerPinAndRouteInMap_origin",
                                      location = origin,
                                      markerType = MarkerType.RIDE_DETAILS_PASSENGER_ORIGIN(bitmap)
                                )
                            }
                        }

                        runOnUiThread {
                            mapProvider.addMarker(
                                  key = "drawCurrentPassengerPinAndRouteInMap_destination",
                                  location = destination,
                                  markerType = MarkerType.RIDE_DETAILS_DESTINATION
                            )
                        }
                    }

                    override fun drawOtherDriverPinAndRouteInMap(
                        origin: Location?,
                        destination: Location?,
                        pickupPoint: Location?,
                        dropOffPoint: Location?,
                        polyline: String?
                    ) {
                        runOnUiThread {
                            pickupPoint?.let {
                                mapProvider.addMarker(
                                      groupKey = nonConfirmedMatchOverlayKeyPrefix,
                                      key = "DriverPinAndRouteInMap_origin",
                                      location = it,
                                      markerType = MarkerType.RIDE_DETAILS_DRIVER_PICKUP
                                )
                            }

                            dropOffPoint?.let {
                                mapProvider.addMarker(
                                      groupKey = nonConfirmedMatchOverlayKeyPrefix,
                                      key = "DriverPinAndRouteInMap_destination",
                                      location = it,
                                      markerType = MarkerType.RIDE_DETAILS_DROPOFF_POINT
                                )
                            }

                            if (!polyline.isNullOrEmpty()) {
                                mapProvider.addRoute(
                                      groupKey = nonConfirmedMatchOverlayKeyPrefix,
                                      key = "DriverPinAndRouteInMap_polyline",
                                      polylineOptions = PolylineOptions()
                                            .addAll(PolyUtil.decode(polyline))
                                            .color(ContextCompat.getColor(baseContext, R.color.primary))
                                            .width(dp2px(ROUTE_WIDTH))
                                )
                            }

                            if (origin != null && pickupPoint != null) {
                                mapProvider.addWalkingRoute("walkingRouteToPickupPoint", origin, pickupPoint)
                            }

                            if (dropOffPoint != null && destination != null) {
                                mapProvider.addWalkingRoute("walkingRouteFromDropOffPoint", dropOffPoint, destination)
                            }
                        }
                    }

                    override fun drawOtherPassengerPinAndRouteInMap(
                        currentUser: UserModel,
                        pickupPoint: Location?,
                        dropOffPoint: Location?,
                        polyline: String?
                    ) {

                        pickupPoint?.let {
                            baseContext.loadAvatarWithBorder(currentUser.avatarUrl) { bitmap ->
                                runOnUiThread {
                                    mapProvider.addMarker(
                                          groupKey = nonConfirmedMatchOverlayKeyPrefix,
                                          key = "PassengerPinAndRouteInMap_origin",
                                          location = it,
                                          markerType = MarkerType.RIDE_DETAILS_PASSENGER_PICKUP(bitmap)
                                    )
                                }
                            }
                        }

                        runOnUiThread {
                            dropOffPoint?.let {
                                mapProvider.addMarker(
                                      groupKey = nonConfirmedMatchOverlayKeyPrefix,
                                      key = "PassengerPinAndRouteInMap_destination",
                                      location = it,
                                      markerType = MarkerType.RIDE_DETAILS_DROPOFF_POINT
                                )
                            }

                            if (!polyline.isNullOrEmpty()) {
                                mapProvider.addRoute(
                                      groupKey = nonConfirmedMatchOverlayKeyPrefix,
                                      key = "PassengerPinAndRouteInMap_polyline",
                                      polylineOptions = PolylineOptions()
                                            .addAll(PolyUtil.decode(polyline))
                                            .color(ContextCompat.getColor(baseContext, R.color.primary))
                                            .width(dp2px(ROUTE_WIDTH))
                                )
                            }
                        }
                    }

                    override fun updateList(items: List<RideDetailsViewModel>) {
                        runOnUiThread {
                            adapterHorizontal.setItems(items)
                        }
                    }

                    override fun updateAndShowConfirmedList(items: List<RideDetailsViewModel.RideDetailsDriverConfirmedViewModel>) {
                        runOnUiThread {

                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                            textHeaderBottomSheet.text = resources.getQuantityString(
                                  R.plurals.ride_details_driver_bottom_sheet_header,
                                  items.size,
                                  items.size
                            )

                            adapterBottomList.setItems(items)
                            bottomSheet.isGone = false
                            listContent.setMarginBottom(MARGIN_BOTTOM_SHEET_FROM_BOTTOM)
                            setCollapsedBottomSheetStateMapPadding()
                        }
                    }

                    override fun hideConfirmedList() {
                        runOnUiThread {
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                            bottomSheet.isGone = true
                            listContent.setMarginBottom(0)
                            adapterBottomList.setItems(emptyList())

                            mapProvider.setPadding(
                                  left = dp2px(MAP_RIGHT_AND_LEFT_PADDING).toInt(),
                                  top = dp2px(MAP_RIGHT_AND_LEFT_PADDING).toInt(),
                                  right = dp2px(0).toInt(),
                                  bottom = listContent.height
                            )
                        }
                    }

                    override fun cleanMatchesMapElements() {
                        runOnUiThread {
                            mapProvider.removeAllByGroupKey(nonConfirmedMatchOverlayKeyPrefix)
                        }
                    }

                    override fun cleanConfirmedMapElements() {
                        runOnUiThread {
                            mapProvider.removeAllByGroupKey(confirmedMatchOverlayKeyPrefix)
                        }
                    }

                    override fun drawConfirmedPassengersPickupPointsOnMap(markerModels: List<ConfirmedPassengerMapMarkerModel>) {
                        markerModels.forEachIndexed { index, markerModel ->
                            markerModel.location?.let { location ->

                                baseContext.loadAvatarWithBorder(markerModel.passenger.avatarUrl) { bitmap ->
                                    runOnUiThread {
                                        mapProvider.addMarker(
                                              groupKey = confirmedMatchOverlayKeyPrefix,
                                              key = "PassengerPinAndRouteInMap_pickup_$index",
                                              location = location,
                                              markerType = MarkerType.RIDE_DETAILS_PASSENGER_PICKUP(bitmap)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    override fun drawConfirmedPassengersSharedRoutesOnMap(polylines: List<String>) {
                        runOnUiThread {
                            polylines.forEachIndexed { index, polyline ->
                                if (polyline.isNotEmpty()) {
                                    mapProvider.addRoute(
                                          groupKey = confirmedMatchOverlayKeyPrefix,
                                          key = "PassengerPinAndRouteInMap_polyline_$index",
                                          polylineOptions = PolylineOptions()
                                                .addAll(PolyUtil.decode(polyline))
                                                .color(ContextCompat.getColor(baseContext, R.color.primary))
                                                .width(dp2px(ROUTE_WIDTH))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private var currentRideDetailsActivityModel: RideDetailsActivityModel? = null

    private val listContent: RecyclerView by bindView(R.id.ride_details_horizontal_list)
    private val textHeaderBottomSheet: TextView by bindView(R.id.ride_details_bottom_peek_header_text)
    private val listContentBottom: RecyclerView by bindView(R.id.ride_details_bottom_list)
    private val bottomSheet: View by bindView(R.id.ride_details_bottom_sheet)
    private val closeBtn: View by bindView(R.id.ride_details_close_icon)

    private lateinit var mapProvider: MapProvider
    private val adapterHorizontal = RendererRecyclerViewAdapter()
    private val adapterBottomList = RendererRecyclerViewAdapter()
    private val bottomSheetBehavior: BottomSheetBehaviorWithHalfExpansion<View> by lazy {
        (BottomSheetBehavior.from(bottomSheet) as BottomSheetBehaviorWithHalfExpansion).apply {
            changeListener = object : BottomSheetBehaviorChangeListener {
                override fun onStateChanged(state: Int) {
                    when (state) {
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            val visibleIndex: Int =
                                  (listContent.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                            actions.onNext(
                                  RideDetailsPresenter.Action.BottomSheetCollapsed(visibleIndex)
                            )
                            setCollapsedBottomSheetStateMapPadding()
                        }

                        BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                            actions.onNext(
                                  RideDetailsPresenter.Action.BottomSheetExpanded
                            )
                            setExpandedBottomSheetStateMapPadding()
                        }
                    }
                }
            }
        }
    }

    override fun initializeBeforePresenter() {
        currentRideDetailsActivityModel = retrieveArgument()
        Timber.d("currentRideDetailsActivityModel:${currentRideDetailsActivityModel.toString()}")

        setActivityTag(currentRideDetailsActivityModel?.rideId)
    }

    override fun initializeViewListeners() {
        registerAdapterRenderers()

        adapterHorizontal.enableDiffUtil()
        adapterBottomList.enableDiffUtil()

        listContent.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        listContentBottom.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        listContent.itemAnimator = DefaultItemAnimator()
        listContentBottom.itemAnimator = DefaultItemAnimator()

        //Snap scroll for list
        LinearSnapHelper().attachToRecyclerView(listContent)

        listContent.adapter = adapterHorizontal
        listContentBottom.adapter = adapterBottomList

        mapProvider = MapProvider.builder()
              .makeEverythingVisible()
              .addPadding()
              .animateCamera()
              .setDefaultLocationCurrentLocation(DEFAULT_ZOOM_FOR_CURRENT_LOCATION)
              .disposeBy(disposables)
              .build(supportFragmentManager)

        mapProvider.startWith(R.id.ride_details_google_map_fragment)

        closeBtn.setOnClickListener {
            actions.onNext(RideDetailsPresenter.Action.CloseButtonClicked)
        }

        textHeaderBottomSheet.setOnClickListener {
            //            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }

        listContent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                actions.onNext(
                      RideDetailsPresenter.Action.HorizontalListScrolled(
                            (listContent.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                      )
                )
            }
        })
    }

    private fun setCollapsedBottomSheetStateMapPadding() {
        mapProvider.setPadding(
              left = dp2px(MAP_RIGHT_AND_LEFT_PADDING).toInt(),
              top = dp2px(MAP_RIGHT_AND_LEFT_PADDING).toInt(),
              right = dp2px(0).toInt(),
              bottom = listContent.height + listContent.marginBottom
        )
    }

    private fun setExpandedBottomSheetStateMapPadding() {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        mapProvider.setPadding(
              left = dp2px(MAP_RIGHT_AND_LEFT_PADDING).toInt(),
              top = dp2px(MAP_RIGHT_AND_LEFT_PADDING).toInt(),
              right = dp2px(0).toInt(),
              bottom = size.y / 2
        )
    }

    private fun registerAdapterRenderers() {
        adapterHorizontal.registerRenderer(
              ViewBinder(
                    R.layout.ride_details_card_renderer,
                    RideDetailsViewModel.RideDetailsPassengerRecommendationViewModel::class.java,
                    RideDetailsPassengerRecommendedRenderer(actions)
              )
        )
        adapterHorizontal.registerRenderer(
              ViewBinder(
                    R.layout.ride_details_card_renderer,
                    RideDetailsViewModel.RideDetailsPassengerOfferedViewModel::class.java,
                    RideDetailsPassengerOfferedRenderer(actions)
              )
        )
        adapterHorizontal.registerRenderer(
              ViewBinder(
                    R.layout.ride_details_card_renderer,
                    RideDetailsViewModel.RideDetailsPassengerRequestedViewModel::class.java,
                    RideDetailsPassengerRequestedRenderer(actions)
              )
        )
        adapterHorizontal.registerRenderer(
              ViewBinder(
                    R.layout.ride_details_card_renderer,
                    RideDetailsViewModel.RideDetailsPassengerConfirmedViewModel::class.java,
                    RideDetailsPassengerConfirmedRenderer(actions)
              )
        )

        adapterHorizontal.registerRenderer(
              ViewBinder(
                    R.layout.ride_details_card_renderer,
                    RideDetailsViewModel.RideDetailsDriverRecommendationViewModel::class.java,
                    RideDetailsDriverRecommendedRenderer(actions)
              )
        )
        adapterHorizontal.registerRenderer(
              ViewBinder(
                    R.layout.ride_details_card_renderer,
                    RideDetailsViewModel.RideDetailsDriverOfferedViewModel::class.java,
                    RideDetailsDriverOfferedRenderer(actions)
              )
        )
        adapterHorizontal.registerRenderer(
              ViewBinder(
                    R.layout.ride_details_card_renderer,
                    RideDetailsViewModel.RideDetailsDriverRequestedViewModel::class.java,
                    RideDetailsDriverRequestedRenderer(actions)
              )
        )
        adapterHorizontal.registerRenderer(
              ViewBinder(
                    R.layout.ride_details_empty_card_renderer,
                    RideDetailsViewModel.RideDetailsDriverEmptyViewModel::class.java,
                    RideDetailsDriverEmptyRenderer(actions)
              )
        )
        adapterHorizontal.registerRenderer(
              ViewBinder(
                    R.layout.ride_details_empty_card_renderer,
                    RideDetailsViewModel.RideDetailsPassengerEmptyViewModel::class.java,
                    RideDetailsPassengerEmptyRenderer(actions)
              )
        )
        adapterBottomList.registerRenderer(
              ViewBinder(
                    R.layout.ride_details_confirmed_passenger_renderer,
                    RideDetailsViewModel.RideDetailsDriverConfirmedViewModel::class.java,
                    RideDetailsDriverConfirmedRenderer(actions)
              )
        )
    }

    companion object {
        private const val nonConfirmedMatchOverlayKeyPrefix = "ride_details_drawOther"
        private const val confirmedMatchOverlayKeyPrefix = "ride_details_drawConfirmedOther"
        private const val MAP_RIGHT_AND_LEFT_PADDING = 30
        private const val DEFAULT_ZOOM_FOR_CURRENT_LOCATION: Float = 10.0f
        private const val ROUTE_WIDTH: Int = 5
        private const val MARGIN_BOTTOM_SHEET_FROM_BOTTOM: Int = 80
    }
}
