package com.deftmove.home.renderers

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.deftmove.carpooling.interfaces.authentication.model.UserModel
import com.deftmove.carpooling.interfaces.ride.model.RideForFeed
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.heart.common.extension.isEmpty
import com.deftmove.heart.common.extension.isNotEmpty
import com.deftmove.heart.common.ui.extension.showAvatar
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.home.BuildConfig
import com.deftmove.home.HomePresenter
import com.deftmove.home.R
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewFinder
import io.reactivex.subjects.Subject

class RideFeedRendererPassengerDelegate(
    private val currentUserManager: CurrentUserManager
) : RideFeedRendererDelegateAbstract() {

    override fun bind(
        actions: Subject<PresenterAction>,
        currentUser: UserModel,
        rideForFeed: RideForFeed,
        finder: ViewFinder
    ) {
        super.bind(actions, currentUser, rideForFeed, finder)

        showCountsAndStatus(actions, finder, rideForFeed)

        if (BuildConfig.CONFIG_HAS_PAYMENT) {
            showPrice(finder, rideForFeed)
            hidePriceDescription(finder)
            showRatePrice(finder)
            prepareInfoIconClick(actions, finder)
        } else {
            hidePrice(finder)
        }
    }

    private fun prepareInfoIconClick(actions: Subject<PresenterAction>, finder: ViewFinder) {
        val imgPriceDescriptionIcon: ImageView = finder.find(R.id.ride_feed_item_money_amount_description_icon)
        imgPriceDescriptionIcon.isGone = false
        imgPriceDescriptionIcon.setOnClickListener {
            actions.onNext(HomePresenter.Action.RideCardInfoIconClicked)
        }
    }

    private fun showRatePrice(finder: ViewFinder) {
        val txtRatePrice: TextView = finder.find(R.id.ride_feed_item_money_rate_price_text)
        txtRatePrice.text = txtRatePrice.context.getString(
              R.string.activity_ride_feed_item_base_price,
              currentUserManager.getCustomerModel()?.farePerKm?.formatted()
        )
        txtRatePrice.isGone = false
    }

    private fun hidePriceDescription(
        finder: ViewFinder
    ) {
        finder.find<View>(R.id.ride_feed_item_money_amount_text_description).isGone = true
    }

    private fun showPrice(
        finder: ViewFinder,
        rideForFeed: RideForFeed
    ) {
        finder.find<View>(R.id.ride_feed_item_money_icon).isGone = false
        val txtPrice: TextView = finder.find(R.id.ride_feed_item_money_amount_text)
        txtPrice.text = rideForFeed.sumConfirmedPrice.formatted()
        txtPrice.isGone = false

        val txtPriceEmptyDescription: TextView = finder.find(R.id.ride_feed_item_money_price_empty_description_text)
        txtPriceEmptyDescription.setText(R.string.activity_ride_feed_item_price_description_passenger)
        txtPriceEmptyDescription.isGone = rideForFeed.sumConfirmedPrice.amountCents != 0
    }

    private fun hidePrice(finder: ViewFinder) {
        finder.find<View>(R.id.ride_feed_item_money_icon).isGone = true
        finder.find<View>(R.id.ride_feed_item_money_amount_text).isGone = true
        finder.find<View>(R.id.ride_feed_item_money_price_empty_description_text).isGone = true
        finder.find<View>(R.id.ride_feed_item_money_amount_text_description).isGone = true
        finder.find<View>(R.id.ride_feed_item_money_rate_price_text).isGone = true
        finder.find<View>(R.id.ride_feed_item_money_amount_description_icon).isGone = true
    }

    private fun showCountsAndStatus(
        actions: Subject<PresenterAction>,
        finder: ViewFinder,
        rideForFeed: RideForFeed
    ) {
        val imgState: ImageView = finder.find(R.id.ride_feed_item_states_icon)
        val txtInfo: TextView = finder.find(R.id.ride_feed_item_states_info)
        val txtWarning: TextView = finder.find(R.id.ride_feed_item_states_warning)
        val txtSuccess: TextView = finder.find(R.id.ride_feed_item_states_success)

        val confirmedDriverLayout: View = finder.find(R.id.ride_feed_item_states_passenger_confirm_layout)
        val confirmedDriverAvatar: ImageView = finder.find(R.id.ride_feed_item_states_passenger_confirm_driver_avatar)
        val confirmedDriverName: TextView = finder.find(R.id.ride_feed_item_states_passenger_confirm_driver_name)
        val confirmedDriverLicensePlate: TextView =
              finder.find(R.id.ride_feed_item_states_passenger_confirm_driver_license_plate)
        val confirmedDriverContactButton: Button = finder.find(R.id.ride_feed_item_states_passenger_confirm_driver_contact)

        imgState.setImageResource(R.drawable.ic_car_white_24dp)
        if (rideForFeed.getOffersAndRequestedSum().isNotEmpty()) {
            imgState.background = ContextCompat.getDrawable(imgState.context, R.drawable.rounded_warning_background)
        } else {
            imgState.background = ContextCompat.getDrawable(imgState.context, R.drawable.rounded_primary_background)
        }

        txtSuccess.isGone = true

        if (rideForFeed.confirmedCount.isEmpty() && rideForFeed.getOffersAndRequestedSum().isEmpty()) {

            txtInfo.text = imgState.context.resources.getQuantityString(
                  R.plurals.activity_ride_feed_item_passenger_recommendation_count,
                  rideForFeed.recommendationsCount,
                  rideForFeed.recommendationsCount
            )

            confirmedDriverLayout.isGone = true
            txtWarning.isGone = true
            txtInfo.isGone = false

        } else if (rideForFeed.confirmedCount.isEmpty() && rideForFeed.getOffersAndRequestedSum().isNotEmpty()) {

            if (rideForFeed.requestedCount.isNotEmpty() && rideForFeed.offeredCount.isNotEmpty()) {
                //Show offers/requests
                txtWarning.text = imgState.context.getString(
                      R.string.activity_ride_feed_item_requests_and_offers_count,
                      rideForFeed.getOffersAndRequestedSum()
                )
            } else if (rideForFeed.requestedCount.isNotEmpty()) {
                //show requests
                txtWarning.text = imgState.context.resources.getQuantityString(
                      R.plurals.activity_ride_feed_item_requests_count,
                      rideForFeed.requestedCount,
                      rideForFeed.requestedCount
                )
            } else {
                //show offers
                txtWarning.text = imgState.context.resources.getQuantityString(
                      R.plurals.activity_ride_feed_item_offers_count,
                      rideForFeed.offeredCount,
                      rideForFeed.offeredCount
                )
            }

            confirmedDriverLayout.isGone = true
            txtWarning.isGone = false
            txtInfo.isGone = true

        } else {
            confirmedDriverAvatar.showAvatar(rideForFeed.driver?.avatarUrl)
            confirmedDriverName.text = "${rideForFeed.driver?.firstName} ${rideForFeed.driver?.lastName}"
            confirmedDriverLicensePlate.text = rideForFeed.driver?.carLicensePlate
            confirmedDriverContactButton.setOnClickListener {
                rideForFeed.driver?.let {
                    actions.onNext(HomePresenter.Action.DriverContactClicked(driverPhoneNumber = it.phoneNumber))
                }
            }

            confirmedDriverAvatar.setOnClickListener {
                rideForFeed.driver?.let {
                    actions.onNext(HomePresenter.Action.DriverAvatarClicked(it.id))
                }
            }

            confirmedDriverLayout.isGone = false
            txtWarning.isGone = true
            txtInfo.isGone = true
        }
    }
}
