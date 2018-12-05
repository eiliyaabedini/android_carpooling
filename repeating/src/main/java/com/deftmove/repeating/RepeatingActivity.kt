package com.deftmove.repeating

import android.view.View
import android.widget.CheckBox
import com.deftmove.carpooling.interfaces.ride.create.model.RepeatingRideModel
import com.deftmove.heart.common.ui.ActivityWithPresenter
import com.deftmove.heart.common.ui.ScreenBucket
import com.deftmove.heart.common.ui.extension.bindView
import com.deftmove.heart.interfaces.common.ui.ScreenBucketModel
import com.deftmove.heart.interfaces.navigator.retrieveArgument
import org.koin.androidx.scope.currentScope

class RepeatingActivity : ActivityWithPresenter() {

    override fun getCurrentScreenBucket(): ScreenBucket<*> {
        return object : ScreenBucket<RepeatingActivityPresenter.View>(
              ScreenBucketModel(
                    scope = currentScope,
                    viewLayout = R.layout.activity_repeating,
                    enableDisplayHomeAsUp = true
              )
        ) {
            override fun getPresenterView(): RepeatingActivityPresenter.View {
                return object : RepeatingActivityPresenter.View {
                    override fun getReceivedRepeating(): RepeatingRideModel = launchArgument!!

                    override fun getCurrentSelection(): RepeatingRideModel {
                        return RepeatingRideModel(
                              monday = mondayCheckBox.isChecked,
                              tuesday = tuesdayCheckBox.isChecked,
                              wednesday = wednesdayCheckBox.isChecked,
                              thursday = thursdayCheckBox.isChecked,
                              friday = fridayCheckBox.isChecked,
                              saturday = saturdayCheckBox.isChecked,
                              sunday = sundayCheckBox.isChecked
                        )
                    }

                    override fun preselectDays(repeatingRideModel: RepeatingRideModel) {
                        mondayCheckBox.isChecked = repeatingRideModel.monday
                        tuesdayCheckBox.isChecked = repeatingRideModel.tuesday
                        wednesdayCheckBox.isChecked = repeatingRideModel.wednesday
                        thursdayCheckBox.isChecked = repeatingRideModel.thursday
                        fridayCheckBox.isChecked = repeatingRideModel.friday
                        saturdayCheckBox.isChecked = repeatingRideModel.saturday
                        sundayCheckBox.isChecked = repeatingRideModel.sunday
                    }
                }
            }
        }
    }

    private var launchArgument: RepeatingRideModel? = null

    private val mondayLayout: View by bindView(R.id.repeating_monday_layout)
    private val mondayCheckBox: CheckBox by bindView(R.id.repeating_monday_checkbox)
    private val tuesdayLayout: View by bindView(R.id.repeating_tuesday_layout)
    private val tuesdayCheckBox: CheckBox by bindView(R.id.repeating_tuesday_checkbox)
    private val wednesdayLayout: View by bindView(R.id.repeating_wednesday_layout)
    private val wednesdayCheckBox: CheckBox by bindView(R.id.repeating_wednesday_checkbox)
    private val thursdayLayout: View by bindView(R.id.repeating_thursday_layout)
    private val thursdayCheckBox: CheckBox by bindView(R.id.repeating_thursday_checkbox)
    private val fridayLayout: View by bindView(R.id.repeating_friday_layout)
    private val fridayCheckBox: CheckBox by bindView(R.id.repeating_friday_checkbox)
    private val saturdayLayout: View by bindView(R.id.repeating_saturday_layout)
    private val saturdayCheckBox: CheckBox by bindView(R.id.repeating_saturday_checkbox)
    private val sundayLayout: View by bindView(R.id.repeating_sunday_layout)
    private val sundayCheckBox: CheckBox by bindView(R.id.repeating_sunday_checkbox)
    private val actionButton: View by bindView(R.id.repeating_action_button)

    override fun initializeBeforePresenter() {
        launchArgument = retrieveArgument()
    }

    override fun initializeViewListeners() {
        mondayLayout.setOnClickListener {
            mondayCheckBox.performClick()
        }
        tuesdayLayout.setOnClickListener {
            tuesdayCheckBox.performClick()
        }
        wednesdayLayout.setOnClickListener {
            wednesdayCheckBox.performClick()
        }
        thursdayLayout.setOnClickListener {
            thursdayCheckBox.performClick()
        }
        fridayLayout.setOnClickListener {
            fridayCheckBox.performClick()
        }
        saturdayLayout.setOnClickListener {
            saturdayCheckBox.performClick()
        }
        sundayLayout.setOnClickListener {
            sundayCheckBox.performClick()
        }

        actionButton.setOnClickListener {
            actions.onNext(RepeatingActivityPresenter.Action.ActionButtonClicked)
        }
    }
}
