package com.deftmove.ride.details.renderer

import android.widget.ImageView
import android.widget.TextView
import com.deftmove.carpooling.interfaces.authentication.model.UserModel
import com.deftmove.heart.common.ui.extension.showAvatar
import com.deftmove.heart.common.ui.ui.LoadingButton
import com.deftmove.heart.interfaces.common.DateUtils
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.ride.details.R
import com.deftmove.ride.details.RideDetailsPresenter
import com.deftmove.ride.details.model.RideDetailsViewModel
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewBinder
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewFinder
import io.reactivex.subjects.Subject
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.Date

class RideDetailsDriverConfirmedRenderer(
    private val actions: Subject<PresenterAction>
) : ViewBinder.Binder<RideDetailsViewModel.RideDetailsDriverConfirmedViewModel>, KoinComponent {

    private val dateUtils: DateUtils by inject()
    override fun bindView(
        model: RideDetailsViewModel.RideDetailsDriverConfirmedViewModel,
        finder: ViewFinder,
        payloads: MutableList<Any>
    ) {

        loadAvatar(finder, model.invitation.passenger)
        setUserName(finder, model.invitation.passenger.firstName, model.invitation.passenger.lastName)
        setPickupTime(finder, model.invitation.pickup?.time)
        setPickupAddress(finder, model.invitation.pickup?.address)
        finder.find<LoadingButton>(R.id.ride_details_confirmed_passenger_renderer_contact).apply {
            bind(
                  R.string.ride_details_driver_confirmed_secondary_button,
                  LoadingButton.LoadingButtonType.TEXT,
                  R.color.blue
            )
            setShouldShowProgressBar(false)
            setEndAligned()
            setOnClickListener {
                actions.onNext(RideDetailsPresenter.Action.ContactClicked(model.invitation.passenger))
            }
        }
    }

    private fun loadAvatar(finder: ViewFinder, userModel: UserModel) {
        finder.find<ImageView>(R.id.ride_details_confirmed_passenger_renderer_avatar).apply {
            showAvatar(userModel.avatarUrl)

            setOnClickListener {
                actions.onNext(RideDetailsPresenter.Action.AvatarClicked(userModel))
            }
        }
    }

    private fun setUserName(finder: ViewFinder, firstName: String?, lastName: String?) {
        finder.find<TextView>(R.id.ride_details_confirmed_passenger_renderer_user_name).text = String.format(
              "%s %s",
              firstName ?: "",
              lastName ?: ""
        )
    }

    private fun setPickupTime(finder: ViewFinder, time: Date?) {
        finder.find<TextView>(R.id.ride_details_confirmed_passenger_renderer_time).text = dateUtils.dateToTimeString(time)
    }

    private fun setPickupAddress(finder: ViewFinder, pickupAddress: String?) {
        finder.find<TextView>(R.id.ride_details_confirmed_passenger_renderer_pickup).text = pickupAddress ?: ""
    }
}
