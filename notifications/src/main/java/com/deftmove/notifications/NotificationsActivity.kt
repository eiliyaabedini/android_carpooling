package com.deftmove.notifications

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.deftmove.carpooling.interfaces.notifications.model.NotificationModel
import com.deftmove.heart.common.ui.ActivityWithPresenter
import com.deftmove.heart.common.ui.ScreenBucket
import com.deftmove.heart.common.ui.extension.bindView
import com.deftmove.heart.interfaces.common.ui.ScreenBucketModel
import com.deftmove.notifications.renderer.NotificationsFeedRenderer
import com.github.vivchar.rendererrecyclerviewadapter.RendererRecyclerViewAdapter
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewBinder
import org.koin.androidx.scope.currentScope

class NotificationsActivity : ActivityWithPresenter() {
    override fun getCurrentScreenBucket(): ScreenBucket<*> {
        return object : ScreenBucket<NotificationsPresenter.View>(
              ScreenBucketModel(
                    scope = currentScope,
                    viewLayout = R.layout.activity_notifications,
                    enableDisplayHomeAsUp = true
              )
        ) {
            override fun getPresenterView(): NotificationsPresenter.View {
                return object : NotificationsPresenter.View {

                    override fun showSwipeToRefresh() {
                        runOnUiThread {
                            swipeRefreshLayout.isRefreshing = true
                        }
                    }

                    override fun hideSwipeToRefresh() {
                        runOnUiThread {
                            swipeRefreshLayout.isRefreshing = false
                        }
                    }

                    override fun showItems(items: List<NotificationModel>) {
                        runOnUiThread {
                            if (items.isEmpty()) {
                                //TODO show empty view
                            } else {
                                //TODO hide empty view
                            }

                            adapter.setItems(items)
                        }
                    }
                }
            }
        }
    }

    private val list: RecyclerView by bindView(R.id.notifications_screen_list)
    private val swipeRefreshLayout: SwipeRefreshLayout by bindView(R.id.notifications_screen_list_refresh)

    private val adapter = RendererRecyclerViewAdapter()

    override fun initializeViewListeners() {

        enableSmartElevationForActionBar(list)

        swipeRefreshLayout.setOnRefreshListener {
            actions.onNext(NotificationsPresenter.Action.RefreshRequested)
        }

        adapter.registerRenderer(
              ViewBinder(
                    R.layout.notifications_feed_renderer,
                    NotificationModel::class.java,
                    NotificationsFeedRenderer(actions)
              )
        )

        adapter.enableDiffUtil()
        list.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        list.itemAnimator = DefaultItemAnimator()
        list.adapter = adapter
    }
}
