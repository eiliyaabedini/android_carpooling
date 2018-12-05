package com.deftmove.ride.create

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isGone
import com.deftmove.carpooling.commonui.extension.isActive
import com.deftmove.carpooling.commonui.extension.toText
import com.deftmove.carpooling.interfaces.ride.create.model.CreateRideLaunchModel
import com.deftmove.carpooling.interfaces.ride.create.model.CreateRideModel
import com.deftmove.carpooling.interfaces.ride.model.RideRole
import com.deftmove.heart.common.extension.toRelativeDateString
import com.deftmove.heart.common.extension.toTimeString
import com.deftmove.heart.common.ui.ActivityWithPresenter
import com.deftmove.heart.common.ui.ScreenBucket
import com.deftmove.heart.common.ui.extension.bindView
import com.deftmove.heart.common.ui.extension.setTextOrGone
import com.deftmove.heart.interfaces.common.ui.ScreenBucketModel
import com.deftmove.heart.interfaces.navigator.retrieveArgument
import com.deftmove.ride.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import org.koin.androidx.scope.currentScope
import timber.log.Timber

class CreateRideActivity : ActivityWithPresenter() {
    override fun getCurrentScreenBucket(): ScreenBucket<*> {
        return object : ScreenBucket<CreateRidePresenter.View>(
              ScreenBucketModel(
                    scope = currentScope,
                    viewLayout = R.layout.activity_create_ride,
                    enableDisplayHomeAsUp = true
              )
        ) {
            override fun getPresenterView(): CreateRidePresenter.View {
                return object : CreateRidePresenter.View {

                    override fun getCreateRideLaunchModel(): CreateRideLaunchModel {
                        return defaultCreateRideLaunchModel!!
                    }

                    override fun setViewValues(model: CreateRideModel) {
                        runOnUiThread {
                            if (!model.origin.location.isEmpty() && model.origin.isCurrentLocation) {
                                txtFromName.setText(R.string.address_search_screen_current_location)
                            } else {
                                txtFromName.text = model.origin.location.name
                            }

                            if (!model.destination.location.isEmpty() && model.destination.isCurrentLocation) {
                                txtToName.setText(R.string.address_search_screen_current_location)
                            } else {
                                txtToName.text = model.destination.location.name
                            }

                            txtFromAddress.setTextOrGone(model.origin.location.address)
                            txtToAddress.setTextOrGone(model.destination.location.address)

                            txtTimeDay.setText(model.startTime.toRelativeDateString(this@CreateRideActivity))
                            txtTimeTime.setText(model.startTime.toTimeString(this@CreateRideActivity))

                            repeatingSwitch.isChecked = model.repeat.isActive()
                            repeatingDaysText.isGone = !model.repeat.isActive()
                            if (model.repeat.isActive()) {
                                repeatingDaysText.setTextIfPossible(model.repeat.toText(textUtils))
                            }
                        }
                    }

                    override fun enableContinueButton() {
                        runOnUiThread {
                            btnContinueClicked.isEnabled = true
                        }
                    }

                    override fun disableContinueButton() {
                        runOnUiThread {
                            btnContinueClicked.isEnabled = false
                        }
                    }

                    override fun disableRepeating() {
                        repeatingLayout.isGone = true
                    }
                }
            }
        }
    }

    private val fromLayout: View by bindView(R.id.create_ride_from_location_text)
    private val txtFromName: TextView by bindView(R.id.create_ride_from_location_name_text)
    private val txtFromAddress: TextView by bindView(R.id.create_ride_from_location_address_text)
    private val toLayout: View by bindView(R.id.create_ride_to_location_text)
    private val txtToName: TextView by bindView(R.id.create_ride_to_location_name_text)
    private val txtToAddress: TextView by bindView(R.id.create_ride_to_location_address_text)
    private val txtTimeDay: com.deftmove.carpooling.commonui.ui.CustomTextInputLayout by bindView(R.id.create_ride_time_day)
    private val txtTimeTime: com.deftmove.carpooling.commonui.ui.CustomTextInputLayout by bindView(R.id.create_ride_time_time)
    private val btnContinueClicked: Button by bindView(R.id.create_ride_continue_button)
    private val fabSwapLocations: FloatingActionButton by bindView(R.id.create_ride_swap_locations)
    private val repeatingLayout: View by bindView(R.id.create_ride_repeating_layout)
    private val repeatingSwitch: SwitchCompat by bindView(R.id.create_ride_repeating_switch)
    private val repeatingDaysText: com.deftmove.carpooling.commonui.ui.CustomTextInputLayout by bindView(R.id.create_ride_repeating_days)

    private var defaultCreateRideLaunchModel: CreateRideLaunchModel? = null

    override fun initializeBeforePresenter() {
        defaultCreateRideLaunchModel = retrieveArgument()
        Timber.d("defaultUserRole:${defaultCreateRideLaunchModel.toString()}")

        // Todo move this lines to Presenter
        when (defaultCreateRideLaunchModel?.role) {
            RideRole.PASSENGER -> {
                setTitle(R.string.activity_create_ride_passenger_title)
            }

            RideRole.DRIVER -> {
                if (defaultCreateRideLaunchModel?.rideForUpdate == null) {
                    setTitle(R.string.activity_create_ride_driver_title)
                } else {
                    setTitle(R.string.activity_edit_ride_driver_title)
                }
            }
        }
    }

    override fun initializeViewListeners() {
        fromLayout.setOnClickListener {
            actions.onNext(CreateRidePresenter.Action.FromLocationClicked)
        }

        toLayout.setOnClickListener {
            actions.onNext(CreateRidePresenter.Action.ToLocationClicked)
        }

        disposables += txtTimeDay.disabledClicks()
              .subscribe {
                  actions.onNext(CreateRidePresenter.Action.TimeDayClicked)
              }

        disposables += txtTimeTime.disabledClicks()
              .subscribe {
                  actions.onNext(CreateRidePresenter.Action.TimeTimeClicked)
              }

        btnContinueClicked.setOnClickListener {
            actions.onNext(CreateRidePresenter.Action.ContinueClicked)
        }

        fabSwapLocations.setOnClickListener {
            actions.onNext(CreateRidePresenter.Action.SwapLocationsClicked)
        }

        disposables += Observable.merge(repeatingDaysText.disabledClicks(), repeatingLayout.clicks())
              .subscribe {
                  actions.onNext(CreateRidePresenter.Action.RepeatingClicked)
              }

        repeatingSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                actions.onNext(CreateRidePresenter.Action.RepeatingSwitchCheckChanged(isChecked))
            }
        }
    }
}
