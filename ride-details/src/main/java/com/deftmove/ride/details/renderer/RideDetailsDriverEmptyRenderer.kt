package com.deftmove.ride.details.renderer

import android.widget.TextView
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.ride.details.R
import com.deftmove.ride.details.model.RideDetailsViewModel
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewBinder
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewFinder
import io.reactivex.subjects.Subject

class RideDetailsDriverEmptyRenderer(
    private val actions: Subject<PresenterAction>
) : ViewBinder.Binder<RideDetailsViewModel.RideDetailsDriverEmptyViewModel> {

    override fun bindView(
        model: RideDetailsViewModel.RideDetailsDriverEmptyViewModel,
        finder: ViewFinder,
        payloads: MutableList<Any>
    ) {

        if (model.confirmedPassengerCount == 0) {
            finder.find<TextView>(R.id.ride_details_empty_card_renderer_message)
                  .setText(R.string.ride_details_driver_empty_without_count_text)
        } else {
            finder.find<TextView>(R.id.ride_details_empty_card_renderer_message)
                  .setText(R.string.ride_details_driver_empty_with_count_text)
        }
    }
}
