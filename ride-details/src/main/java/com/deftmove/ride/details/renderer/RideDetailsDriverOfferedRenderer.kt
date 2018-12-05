package com.deftmove.ride.details.renderer

import com.deftmove.heart.common.ui.ui.LoadingButton
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.ride.details.R
import com.deftmove.ride.details.RideDetailsPresenter
import com.deftmove.ride.details.model.RideDetailsViewModel
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewFinder
import io.reactivex.subjects.Subject

class RideDetailsDriverOfferedRenderer(
    private val actions: Subject<PresenterAction>
) : RideDetailsBaseRenderer<RideDetailsViewModel.RideDetailsDriverOfferedViewModel>(actions) {

    override fun bindView(
        model: RideDetailsViewModel.RideDetailsDriverOfferedViewModel,
        finder: ViewFinder,
        payloads: MutableList<Any>
    ) {
        super.bindView(model, finder, payloads)

        finder.find<LoadingButton>(R.id.ride_details_card_renderer_primary_button).apply {
            bind(
                  R.string.ride_details_driver_offered_primary_button,
                  LoadingButton.LoadingButtonType.PRIMARY,
                  isEnabled = false
            )

            setOnClickListener {

            }
        }

        finder.find<LoadingButton>(R.id.ride_details_card_renderer_secondary_button).apply {
            bind(R.string.ride_details_driver_offered_secondary_button, LoadingButton.LoadingButtonType.TEXT, R.color.error)

            setOnClickListener {
                actions.onNext(RideDetailsPresenter.Action.CancelInvitation(model.invitation.id))
            }
        }
    }
}
