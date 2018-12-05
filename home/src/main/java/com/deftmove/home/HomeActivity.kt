package com.deftmove.home

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isGone
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.deftmove.carpooling.commonui.ui.VerticalIcons
import com.deftmove.carpooling.interfaces.OpenCreateRideScreen
import com.deftmove.carpooling.interfaces.common.data.IconsType
import com.deftmove.carpooling.interfaces.notifications.repository.NotificationsApiPagingRepository
import com.deftmove.carpooling.interfaces.ride.model.RideForFeed
import com.deftmove.carpooling.interfaces.ride.model.RideRole
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.heart.common.ui.ActivityWithPresenter
import com.deftmove.heart.common.ui.ScreenBucket
import com.deftmove.heart.common.ui.delegate.BottomSheetDelegate
import com.deftmove.heart.common.ui.extension.bindView
import com.deftmove.heart.common.ui.extension.disablePullToRefreshWhenScrollingHorizontally
import com.deftmove.heart.common.ui.extension.showAvatar
import com.deftmove.heart.common.ui.recyclerview.CirclePagerIndicatorDecoration
import com.deftmove.heart.interfaces.common.ui.ScreenBucketModel
import com.deftmove.heart.interfaces.koin.getAuthenticatedUserScope
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import com.deftmove.home.renderers.RideFeedRenderer
import com.github.vivchar.rendererrecyclerviewadapter.RendererRecyclerViewAdapter
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewBinder
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.currentScope

class HomeActivity : ActivityWithPresenter() {
    override fun getCurrentScreenBucket(): ScreenBucket<*> {
        return object : ScreenBucket<HomePresenter.View>(
              ScreenBucketModel(
                    scope = currentScope,
                    viewLayout = R.layout.activity_home,
                    enableDisplayHomeAsUp = false,
                    loadingLayout = R.id.home_loading_layout
              )
        ) {
            override fun getPresenterView(): HomePresenter.View {
                return object : HomePresenter.View {
                    override fun showLoadMoreIndicator() {
                        runOnUiThread {
                            adapter.showLoadMore()
                        }
                    }

                    override fun showSwipeToRefresh() {
                        runOnUiThread {
                            listRefreshLayout.isRefreshing = true
                        }
                    }

                    override fun hideSwipeToRefresh() {
                        runOnUiThread {
                            listRefreshLayout.isRefreshing = false
                        }
                    }

                    override fun hideEmpty() {
                        runOnUiThread {
                            emptyLayout.isGone = true
                        }
                    }

                    override fun showEmpty(firstName: String) {
                        runOnUiThread {
                            txtEmptyName.text = txtEmptyName.context.getString(
                                  R.string.activity_ride_feed_empty_name,
                                  firstName
                            )

                            emptyLayout.isGone = false
                        }
                    }

                    override fun showItems(items: List<RideForFeed>) {
                        runOnUiThread {
                            adapter.setItems(items)
                        }
                    }

                    override fun showUserAvatarInToolbar(imageUrl: String) {
                        runOnUiThread {
                            toolbarAvatar.showAvatar(imageUrl)
                        }
                    }

                    override fun showInfoBottomSheet() {
                        runOnUiThread {
                            bottomSheetDelegate.expand()
                        }
                    }

                    override fun hideInfoBottomSheet() {
                        runOnUiThread {
                            bottomSheetDelegate.collapse()
                        }
                    }
                }
            }
        }
    }

    private val currentUserManager: CurrentUserManager by inject()
    private val heartNavigator: HeartNavigator by inject()
    private val notificationRepository: NotificationsApiPagingRepository by getAuthenticatedUserScope().inject()

    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val toolbarAvatar: ImageView by bindView(R.id.main_avatar_image_view)
    private val toolbarActionIcons: VerticalIcons by bindView(R.id.main_action_icons)
    private val listRefreshLayout: SwipeRefreshLayout by bindView(R.id.home_content_list_refresh)
    private val listContent: RecyclerView by bindView(R.id.home_content_list)
    private val btnFineRide: Button by bindView(R.id.home_find_ride_button)
    private val btnOfferRide: Button by bindView(R.id.home_offer_ride_button)
    private val viewBottomSheet: View by bindView(R.id.home_bottom_sheet)
    private val btnBottomSheetAction: Button by bindView(R.id.home_bottom_sheet_action_button)
    private val viewBottomSheetBackground: View by bindView(R.id.home_bottom_sheet_background)
    private val emptyLayout: View by bindView(R.id.ride_details_empty_layout)
    private val txtEmptyName: TextView by bindView(R.id.ride_details_empty_name)

    private val bottomSheetDelegate: BottomSheetDelegate by lazy {
        BottomSheetDelegate(this, viewBottomSheet, viewBottomSheetBackground)
    }

    private val adapter = RendererRecyclerViewAdapter()

    override fun initializeBeforePresenter() {
        notificationRepository.fetchNotifications()
    }

    override fun initializeViewListeners() {
        viewBottomSheetBackground.setOnClickListener {
            bottomSheetDelegate.collapse()
        }

        toolbarAvatar.setOnClickListener {
            actions.onNext(HomePresenter.Action.ToolbarAvatarClicked)
        }

        toolbarActionIcons.bind(IconsType.HomeScreenActionBar)

        listRefreshLayout.setOnRefreshListener {
            actions.onNext(HomePresenter.Action.RefreshRequested)
        }

        btnOfferRide.setOnClickListener {
            heartNavigator.getLauncher(OpenCreateRideScreen(RideRole.DRIVER))
                  ?.startActivity()
        }

        btnFineRide.setOnClickListener {
            heartNavigator.getLauncher(OpenCreateRideScreen(RideRole.PASSENGER))
                  ?.startActivity()
        }

        btnBottomSheetAction.setOnClickListener {
            actions.onNext(HomePresenter.Action.RideCardInfoBottomSheetActionButtonClicked)
        }

        adapter.registerRenderer(
              ViewBinder(
                    R.layout.trip_feed_renderer,
                    RideForFeed::class.java,
                    RideFeedRenderer(actions, currentUserManager)
              )
        )
        //        adapter.registerRenderer(LoadMoreViewBinder(R.layout.trip_feed_load_more_renderer))

        adapter.enableDiffUtil()

        //        listContent.addOnScrollListener(object : EndlessScrollRecycleListener() {
        //            override fun onLoadMore(page: Int, totalItemsCount: Int) {
        //                actions.onNext(HomePresenter.Action.LoadMoreRequested)
        //            }
        //        })

        listContent.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false).apply {
            scrollToPositionWithOffset(0, 1)
        }
        listContent.addItemDecoration(CirclePagerIndicatorDecoration())

        listContent.itemAnimator = DefaultItemAnimator()

        listContent.disablePullToRefreshWhenScrollingHorizontally(listRefreshLayout)

        //Snap scroll for list
        LinearSnapHelper().attachToRecyclerView(listContent)

        listContent.adapter = adapter
    }

    override fun getViewToolbar(): Toolbar? = toolbar
}
