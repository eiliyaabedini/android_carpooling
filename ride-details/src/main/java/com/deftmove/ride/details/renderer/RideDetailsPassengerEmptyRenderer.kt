package com.deftmove.ride.details.renderer

import android.widget.TextView
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.ride.details.R
import com.deftmove.ride.details.model.RideDetailsViewModel
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewBinder
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewFinder
import io.reactivex.subjects.Subject

class RideDetailsPassengerEmptyRenderer(
    private val actions: Subject<PresenterAction>
) : ViewBinder.Binder<RideDetailsViewModel.RideDetailsPassengerEmptyViewModel> {

    override fun bindView(
        model: RideDetailsViewModel.RideDetailsPassengerEmptyViewModel,
        finder: ViewFinder,
        payloads: MutableList<Any>
    ) {

        finder.find<TextView>(R.id.ride_details_empty_card_renderer_message)
              .setText(R.string.ride_details_passenger_empty_text)
    }
}
