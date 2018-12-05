package com.deftmove.ride.review

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.deftmove.heart.common.extension.toTimeString
import com.deftmove.heart.common.ui.extension.bindView
import com.deftmove.heart.interfaces.map.Location
import com.deftmove.heart.interfaces.model.SearchAddressPrediction
import com.deftmove.ride.R
import timber.log.Timber
import java.util.Date

class ReviewPickupItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val txtName: TextView by bindView(R.id.create_ride_review_pickup_item_name)
    private val txtAddress: TextView by bindView(R.id.create_ride_review_pickup_item_address)
    private val txtTime: TextView by bindView(R.id.create_ride_review_pickup_item_time)

    init {
        if (!isInEditMode) {
            LayoutInflater.from(context)
                  .inflate(R.layout.activity_creare_ride_review_pickup_item_layout, this, true)
        }
    }

    fun bind(location: Location, time: Date) {
        Timber.d("bind location:$location")

        txtName.text = location.name
        txtAddress.text = location.address
        txtTime.text = time.toTimeString(context)
    }

    fun bind(prediction: SearchAddressPrediction, time: Date) {
        Timber.d("bind prediction:$prediction")

        if (!prediction.location.isEmpty() && prediction.isCurrentLocation) {
            txtName.setText(R.string.address_search_screen_current_location)
        } else {
            txtName.text = prediction.location.name
        }

        txtAddress.text = prediction.location.address
        txtTime.text = time.toTimeString(context)
    }
}
