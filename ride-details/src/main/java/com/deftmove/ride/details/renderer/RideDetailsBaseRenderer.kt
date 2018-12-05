package com.deftmove.ride.details.renderer

import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.isGone
import com.deftmove.carpooling.interfaces.authentication.model.UserModel
import com.deftmove.heart.common.ui.extension.showAvatar
import com.deftmove.heart.interfaces.common.ColorUtils
import com.deftmove.heart.interfaces.common.DateUtils
import com.deftmove.heart.interfaces.common.TextUtils
import com.deftmove.heart.interfaces.common.model.Money
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.ride.details.BuildConfig
import com.deftmove.ride.details.R
import com.deftmove.ride.details.RideDetailsPresenter
import com.deftmove.ride.details.model.RideDetailsViewModel
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewBinder
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewFinder
import io.reactivex.subjects.Subject
import org.koin.core.KoinComponent
import org.koin.core.inject

abstract class RideDetailsBaseRenderer<T : RideDetailsViewModel>(
    private val actions: Subject<PresenterAction>
) : ViewBinder.Binder<T>, KoinComponent {

    private val textUtils: TextUtils by inject()
    private val dateUtils: DateUtils by inject()
    private val colorUtils: ColorUtils by inject()

    override fun bindView(model: T, finder: ViewFinder, payloads: MutableList<Any>) {

        when (model) {
            is RideDetailsViewModel.RideDetailsPassengerRecommendationViewModel -> {
                loadAvatar(finder, model.recommendation.user)
                setUserName(finder, model.recommendation.user.firstName, model.recommendation.user.lastName)
                setDescription(
                      finder,
                      textUtils.getString(
                            R.string.ride_details_passenger_recommended_description,
                            dateUtils.dateToTimeString(model.recommendation.time)
                      ),
                      colorUtils.getColor(R.color.grey)
                )
                setPrice(finder, model.recommendation.grossPrice)
            }

            is RideDetailsViewModel.RideDetailsPassengerOfferedViewModel -> {
                loadAvatar(finder, model.invitation.driver)
                setUserName(finder, model.invitation.driver.firstName, model.invitation.driver.lastName)
                setDescription(
                      finder,
                      textUtils.getString(
                            R.string.ride_details_passenger_offered_description
                      ),
                      colorUtils.getColor(R.color.warning)
                )
                setPrice(finder, model.invitation.grossPrice)
            }

            is RideDetailsViewModel.RideDetailsPassengerRequestedViewModel -> {
                loadAvatar(finder, model.invitation.driver)
                setUserName(finder, model.invitation.driver.firstName, model.invitation.driver.lastName)
                setDescription(
                      finder,
                      textUtils.getString(
                            R.string.ride_details_passenger_requested_description,
                            dateUtils.dateToTimeString(model.invitation.pickup?.time)
                      ),
                      colorUtils.getColor(R.color.grey)
                )
                setPrice(finder, model.invitation.grossPrice)
            }

            is RideDetailsViewModel.RideDetailsPassengerConfirmedViewModel -> {
                loadAvatar(finder, model.invitation.driver)
                setUserName(finder, model.invitation.driver.firstName, model.invitation.driver.lastName)
                setDescription(
                      finder,
                      textUtils.getString(
                            R.string.ride_details_passenger_confirmed_description,
                            dateUtils.dateToTimeString(model.invitation.pickup?.time)
                      ),
                      colorUtils.getColor(R.color.grey)
                )
                setPrice(finder, model.invitation.grossPrice)
            }

            is RideDetailsViewModel.RideDetailsDriverRecommendationViewModel -> {
                loadAvatar(finder, model.recommendation.user)
                setUserName(finder, model.recommendation.user.firstName, model.recommendation.user.lastName)
                setDescription(
                      finder,
                      textUtils.getString(
                            R.string.ride_details_driver_recommended_description,
                            dateUtils.dateToTimeString(model.recommendation.time)
                      ),
                      colorUtils.getColor(R.color.grey)
                )
                setPrice(finder, model.recommendation.grossPrice)
            }

            is RideDetailsViewModel.RideDetailsDriverRequestedViewModel -> {
                loadAvatar(finder, model.invitation.passenger)
                setUserName(finder, model.invitation.passenger.firstName, model.invitation.passenger.lastName)
                setDescription(
                      finder,
                      textUtils.getString(
                            R.string.ride_details_driver_requested_description
                      ),
                      colorUtils.getColor(R.color.grey)
                )
                setPrice(finder, model.invitation.grossPrice)
            }

            is RideDetailsViewModel.RideDetailsDriverOfferedViewModel -> {
                loadAvatar(finder, model.invitation.passenger)
                setUserName(finder, model.invitation.passenger.firstName, model.invitation.passenger.lastName)
                setDescription(
                      finder,
                      textUtils.getString(
                            R.string.ride_details_driver_offered_description,
                            dateUtils.dateToTimeString(model.invitation.pickup?.time)
                      ),
                      colorUtils.getColor(R.color.grey)
                )
                setPrice(finder, model.invitation.grossPrice)
            }

            is RideDetailsViewModel.RideDetailsDriverConfirmedViewModel -> {
                loadAvatar(finder, model.invitation.passenger)
                setUserName(finder, model.invitation.passenger.firstName, model.invitation.passenger.lastName)
                setDescription(
                      finder,
                      textUtils.getString(
                            R.string.ride_details_driver_requested_description,
                            dateUtils.dateToTimeString(model.invitation.pickup?.time)
                      ),
                      colorUtils.getColor(R.color.grey)
                )
                setPrice(finder, model.invitation.grossPrice)
            }
        }
    }

    private fun loadAvatar(finder: ViewFinder, userModel: UserModel) {
        finder.find<ImageView>(R.id.ride_details_card_renderer_avatar).apply {
            showAvatar(userModel.avatarUrl)

            setOnClickListener {
                actions.onNext(RideDetailsPresenter.Action.AvatarClicked(userModel))
            }
        }
    }

    private fun setUserName(finder: ViewFinder, firstName: String?, lastName: String?) {
        finder.find<TextView>(R.id.ride_details_card_renderer_user_name).text = String.format(
              "%s %s",
              firstName ?: "",
              lastName ?: ""
        )
    }

    private fun setDescription(finder: ViewFinder, description: String, @ColorInt color: Int) {
        finder.find<TextView>(R.id.ride_details_card_renderer_description).apply {
            setTextColor(color)
            text = description
        }
    }

    private fun setPrice(finder: ViewFinder, price: Money?) {
        finder.find<TextView>(R.id.ride_details_card_renderer_price).apply {
            if (BuildConfig.CONFIG_HAS_PAYMENT) {
                text = price?.formatted() ?: ""
                isGone = false
            } else {
                isGone = true
            }
        }
    }
}
