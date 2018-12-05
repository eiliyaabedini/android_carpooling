package com.deftmove.home.renderers

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.deftmove.carpooling.interfaces.authentication.model.UserModel
import com.deftmove.carpooling.interfaces.ride.model.RideForFeed
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.heart.common.extension.isEmpty
import com.deftmove.heart.common.extension.isNotEmpty
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.home.BuildConfig
import com.deftmove.home.HomePresenter
import com.deftmove.home.R
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewFinder
import io.reactivex.subjects.Subject

class RideFeedRendererDriverDelegate(
    private val currentUserManager: CurrentUserManager
) : RideFeedRendererDelegateAbstract() {

    override fun bind(
        actions: Subject<PresenterAction>,
        currentUser: UserModel,
        rideForFeed: RideForFeed,
        finder: ViewFinder
    ) {
        super.bind(actions, currentUser, rideForFeed, finder)

        showCountsAndStatus(finder, rideForFeed)

        if (BuildConfig.CONFIG_HAS_PAYMENT) {
            showPrice(finder, rideForFeed)
            showPriceDescription(finder, rideForFeed)
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

    private fun showPriceDescription(
        finder: ViewFinder,
        rideForFeed: RideForFeed
    ) {
        val txtPriceDescription: TextView = finder.find(R.id.ride_feed_item_money_amount_text_description)
        txtPriceDescription.isGone = rideForFeed.confirmedCount.isEmpty()
        txtPriceDescription.text = if (rideForFeed.getOffersAndRequestedSum().isEmpty()) {
            txtPriceDescription.context.getString(R.string.activity_ride_feed_item_text_description_without_pending)
        } else {
            txtPriceDescription.context.getString(R.string.activity_ride_feed_item_text_description_with_pending)
        }
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
        txtPriceEmptyDescription.setText(R.string.activity_ride_feed_item_price_description_driver)
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
        finder: ViewFinder,
        rideForFeed: RideForFeed
    ) {
        val imgState: ImageView = finder.find(R.id.ride_feed_item_states_icon)
        val txtInfo: TextView = finder.find(R.id.ride_feed_item_states_info)
        val txtWarning: TextView = finder.find(R.id.ride_feed_item_states_warning)
        val txtSuccess: TextView = finder.find(R.id.ride_feed_item_states_success)

        imgState.setImageResource(R.drawable.ic_account_white_24dp)
        if (rideForFeed.getOffersAndRequestedSum().isNotEmpty()) {
            imgState.background = ContextCompat.getDrawable(imgState.context, R.drawable.rounded_warning_background)
        } else {
            imgState.background = ContextCompat.getDrawable(imgState.context, R.drawable.rounded_primary_background)
        }

        if (rideForFeed.getOffersAndRequestedSum().isNotEmpty() || rideForFeed.confirmedCount.isNotEmpty()) {

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

            txtSuccess.text = imgState.context.resources.getQuantityString(
                  R.plurals.activity_ride_feed_item_confirmed_passenger_count,
                  rideForFeed.confirmedCount,
                  rideForFeed.confirmedCount
            )

            txtWarning.isGone = rideForFeed.getOffersAndRequestedSum().isEmpty()
            txtSuccess.isGone = rideForFeed.confirmedCount.isEmpty()
            txtInfo.isGone = true

        } else {
            txtInfo.text = imgState.context.resources.getQuantityString(
                  R.plurals.activity_ride_feed_item_driver_recommendation_count,
                  rideForFeed.recommendationsCount,
                  rideForFeed.recommendationsCount
            )

            txtWarning.isGone = true
            txtSuccess.isGone = true
            txtInfo.isGone = false
        }
    }
}
