package com.deftmove.debugtools.matchmaker

import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.isVisible
import com.deftmove.debugtools.R
import com.deftmove.heart.common.extension.toRelativeDateTimeString
import com.deftmove.heart.common.ui.ActivityWithPresenter
import com.deftmove.heart.common.ui.ScreenBucket
import com.deftmove.heart.common.ui.extension.bindView
import com.deftmove.heart.interfaces.common.ui.ScreenBucketModel
import com.deftmove.heart.interfaces.map.Location
import com.jakewharton.rxbinding3.view.globalLayouts
import com.jakewharton.rxbinding3.widget.itemSelections
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.plusAssign
import org.koin.androidx.scope.currentScope
import java.util.Date

class MatchMakerActivity : ActivityWithPresenter() {

    override fun getCurrentScreenBucket(): ScreenBucket<*> {
        return object : ScreenBucket<MatchMakerPresenter.View>(
              ScreenBucketModel(
                    scope = currentScope,
                    viewLayout = R.layout.match_maker_screen,
                    enableDisplayHomeAsUp = false
              )
        ) {
            override fun getPresenterView(): MatchMakerPresenter.View {
                return object : MatchMakerPresenter.View {

                    override fun makeDriverSpinner(list: List<MockMakerUser>) {
                        runOnUiThread {
                            val statuses = daxStatuses.map { it.name }.map { it.toLowerCase() }.map { it.capitalize() }
                            val spinner = makeSpinner(list.map { it.name }, 0, statuses,
                                  Consumer { (userIndex, statusIndex) ->
                                      actions.onNext(
                                            MatchMakerPresenter.Action.DaxChanged(
                                                  daxStatuses[statusIndex],
                                                  list[userIndex]
                                            )
                                      )
                                  })

                            daxSpinnersLayout.addView(
                                  spinner,
                                  prepareSpinnerLayoutParams()
                            )
                        }
                    }

                    override fun makePassengerSpinner(list: List<MockMakerUser>, index: Int, selectedUserIndex: Int) {
                        runOnUiThread {
                            val spinner = makeSpinner(list.map { it.name },
                                  selectedUserIndex,
                                  paxStatuses.map { it.name }.map { it.toLowerCase() }.map { it.capitalize() },
                                  Consumer { (userIndex, statusIndex) ->
                                      actions.onNext(
                                            MatchMakerPresenter.Action.PaxChanged(
                                                  index,
                                                  list[userIndex].copy(status = paxStatuses[statusIndex])
                                            )
                                      )
                                  })

                            disposables += paxScrollView.globalLayouts().take(1)
                                  .subscribe { paxScrollView.fullScroll(View.FOCUS_DOWN) }

                            paxSpinnersLayout.addView(
                                  spinner,
                                  -1,
                                  prepareSpinnerLayoutParams()
                            )

                        }
                    }

                    override fun setPickedDate(date: Date) {
                        runOnUiThread {
                            pickDateValue.text = date.toRelativeDateTimeString(baseContext)
                            pickDateValue.isVisible = true
                        }
                    }

                    override fun setOrigin(location: Location) {
                        runOnUiThread {
                            pickOriginValue.text = location.address
                            pickOriginValue.isVisible = true
                        }
                    }

                    override fun setDestination(location: Location) {
                        runOnUiThread {
                            pickDestinationValue.text = location.address
                            pickDestinationValue.isVisible = true
                        }
                    }
                }
            }
        }
    }

    private val pickDateLayout: View by bindView(R.id.pick_date_layout)
    private val pickDateValue: TextView by bindView(R.id.pick_date_sub)
    private val pickOriginLayout: View by bindView(R.id.pick_origin_layout)
    private val pickOriginValue: TextView by bindView(R.id.pick_origin_sub)
    private val pickDestinationLayout: View by bindView(R.id.pick_destination_layout)
    private val pickDestinationValue: TextView by bindView(R.id.pick_destination_sub)
    private val daxSpinnersLayout: LinearLayout by bindView(R.id.match_maker_dialog_dax_spinners)
    private val paxSpinnersLayout: LinearLayout by bindView(R.id.match_maker_dialog_pax_spinners)
    private val paxScrollView: ScrollView by bindView(R.id.match_maker_dialog_pax_scrollview)
    private val imgAddMorePax: ImageView by bindView(R.id.match_maker_dialog_add_pax)
    private val imgAddMoreShufflePax: Button by bindView(R.id.match_maker_dialog_add_shuffle_paxes)
    private val btnCreate: Button by bindView(R.id.match_maker_dialog_create_button)
    private val btnClose: Button by bindView(R.id.match_maker_dialog_close_button)

    override fun initializeViewListeners() {

        pickDateLayout.setOnClickListener {
            actions.onNext(MatchMakerPresenter.Action.DatePickerClicked)
        }

        pickOriginLayout.setOnClickListener {
            actions.onNext(MatchMakerPresenter.Action.OriginPickerClicked)
        }

        pickDestinationLayout.setOnClickListener {
            actions.onNext(MatchMakerPresenter.Action.DestinationPickerClicked)
        }

        imgAddMorePax.setOnClickListener {
            actions.onNext(MatchMakerPresenter.Action.AddMorePax)
        }

        imgAddMoreShufflePax.setOnClickListener {
            actions.onNext(MatchMakerPresenter.Action.ShufflePax)
        }

        btnCreate.setOnClickListener {
            actions.onNext(MatchMakerPresenter.Action.CreateMatches)
        }

        btnClose.setOnClickListener {
            actions.onNext(MatchMakerPresenter.Action.CloseScreen)
        }
    }

    private fun prepareSpinnerLayoutParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun makeSpinner(
        userList: List<String>, selectedIndex: Int, statusList: List<String>,
        consumer: Consumer<Pair<Int, Int>>
    ): View {
        val view = layoutInflater.inflate(R.layout.match_maker_user_view, null)

        val userAdapter = ArrayAdapter<String>(baseContext, android.R.layout.simple_spinner_item)
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userAdapter.addAll(userList)
        val userSpinner = view.findViewById<Spinner>(R.id.match_maker_spinner_user)
        userSpinner.adapter = userAdapter
        userSpinner.setSelection(selectedIndex)

        val statusAdapter = ArrayAdapter<String>(baseContext, android.R.layout.simple_spinner_item)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        statusAdapter.addAll(statusList)
        val statusSpinner = view.findViewById<Spinner>(R.id.match_maker_spinner_status)
        statusSpinner.adapter = statusAdapter

        val skipAmount = if (selectedIndex > 0) 0L else 1L
        disposables += Observables.combineLatest(
              userSpinner.itemSelections().skipInitialValue().skip(skipAmount),
              statusSpinner.itemSelections()
        )
              .subscribe(consumer)

        return view
    }

    companion object {
        val paxStatuses = listOf(
              InvitationStatus.RECOMMENDED,
              InvitationStatus.REQUESTED,
              InvitationStatus.OFFERED,
              InvitationStatus.CONFIRMED
        )
        val daxStatuses = listOf(
              RideStatus.SCHEDULED,
              RideStatus.STARTED
        )
    }
}
