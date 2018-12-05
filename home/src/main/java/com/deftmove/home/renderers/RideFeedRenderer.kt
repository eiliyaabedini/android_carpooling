package com.deftmove.home.renderers

import android.view.View
import com.deftmove.carpooling.interfaces.ride.model.RideForFeed
import com.deftmove.carpooling.interfaces.ride.model.RideRole
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.home.HomePresenter
import com.deftmove.home.R
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewBinder
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewFinder
import io.reactivex.subjects.Subject

class RideFeedRenderer(
    private val actions: Subject<PresenterAction>,
    private val currentUserManager: CurrentUserManager
) : ViewBinder.Binder<RideForFeed> {

    private lateinit var delegate: RideFeedRendererDelegateAbstract

    override fun bindView(rideForFeed: RideForFeed, finder: ViewFinder, payloads: MutableList<Any>) {
        delegate = when (rideForFeed.role) {
            RideRole.DRIVER -> RideFeedRendererDriverDelegate(currentUserManager)
            RideRole.PASSENGER -> RideFeedRendererPassengerDelegate(currentUserManager)
        }

        delegate.bind(actions, currentUserManager.getUserModel()!!, rideForFeed, finder)

        finder.find<View>(R.id.ride_feed_renderer_parent).setOnClickListener {
            actions.onNext(HomePresenter.Action.RideCardClicked(rideId = rideForFeed.id))
        }
    }
}
