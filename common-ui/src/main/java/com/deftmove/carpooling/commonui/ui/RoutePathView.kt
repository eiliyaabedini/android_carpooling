package com.deftmove.carpooling.commonui.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.deftmove.carpooling.commonui.R
import com.deftmove.heart.common.ui.extension.bindView
import com.deftmove.heart.common.ui.extension.laidOut
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber

class RoutePathView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val circlesLayout: LinearLayout by bindView(R.id.route_path_view_circles)

    private val pickupPointHeights: MutableList<Int> = mutableListOf()

    private val disposables = CompositeDisposable()

    init {
        if (!isInEditMode) {
            LayoutInflater.from(context)
                  .inflate(R.layout.route_path_view_layout, this, true)
        }
    }

    //View here has to be laid out already
    fun addPickupPoint(pickupView: View) {
        Timber.d("ffffff addPickupPoint called")

        disposables += pickupView.laidOut()
              .doOnSuccess {
                  pickupPointHeights.add(pickupView.height)

                  //TODO optimise it to run only once after a debounce
                  updatePickupPoints()
              }
              .subscribe()
    }

    private fun updatePickupPoints() {
        circlesLayout.removeAllViews()

        pickupPointHeights.forEach { pickupPointHeght ->

            Timber.d("ffffff updatePickupPoints pickupPoint pickupPointHeght:$pickupPointHeght")

            val view = LayoutInflater.from(context).inflate(R.layout.route_path_single_path_layout, null)

            circlesLayout.addView(view, LayoutParams(LayoutParams.WRAP_CONTENT, pickupPointHeght))
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        disposables.clear()
    }
}
