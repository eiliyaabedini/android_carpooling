package com.deftmove.ride.details.renderer

import com.deftmove.heart.common.ui.ui.LoadingButton
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.ride.details.R
import com.deftmove.ride.details.RideDetailsPresenter
import com.deftmove.ride.details.model.RideDetailsViewModel
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewFinder
import io.reactivex.subjects.Subject

class RideDetailsDriverRecommendedRenderer(
    private val actions: Subject<PresenterAction>
) : RideDetailsBaseRenderer<RideDetailsViewModel.RideDetailsDriverRecommendationViewModel>(actions) {

    override fun bindView(
        model: RideDetailsViewModel.RideDetailsDriverRecommendationViewModel,
        finder: ViewFinder,
        payloads: MutableList<Any>
    ) {
        super.bindView(model, finder, payloads)


        finder.find<LoadingButton>(R.id.ride_details_card_renderer_primary_button).apply {
            bind(R.string.ride_details_driver_recommended_primary_button, LoadingButton.LoadingButtonType.PRIMARY)

            setOnClickListener {
                actions.onNext(RideDetailsPresenter.Action.CreateInvitation(model.recommendation.id))
            }
        }
        finder.find<LoadingButton>(R.id.ride_details_card_renderer_secondary_button).apply {
            bind(0, LoadingButton.LoadingButtonType.NONE)

            setOnClickListener {

            }
        }
    }
}
