package com.deftmove.repeating

import com.deftmove.carpooling.interfaces.ride.create.model.RepeatingRideModel
import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonAction
import com.deftmove.heart.interfaces.common.presenter.PresenterView
import timber.log.Timber

class RepeatingActivityPresenter(
    private val reactiveTransformer: ReactiveTransformer
) : Presenter<RepeatingActivityPresenter.View>() {

    private lateinit var receivedRepeatingRideModel: RepeatingRideModel

    override fun initialise() {
        receivedRepeatingRideModel = view?.getReceivedRepeating()!!

        view?.preselectDays(receivedRepeatingRideModel)

        commonView?.actions()
              ?.subscribeOn(reactiveTransformer.ioScheduler())
              ?.subscribeSafeWithShowingErrorContent { action ->
                  Timber.d("action:%s", action)
                  when (action) {
                      is PresenterCommonAction.ErrorRetryClicked -> {
                          commonView?.showContent()
                      }

                      is Action.ActionButtonClicked -> {
                          commonView?.closeScreenWithResult(
                                view?.getCurrentSelection() ?: RepeatingRideModel()
                          )
                      }
                  }
              }
    }

    interface View : PresenterView {
        fun getReceivedRepeating(): RepeatingRideModel
        fun getCurrentSelection(): RepeatingRideModel

        fun preselectDays(repeatingRideModel: RepeatingRideModel)
    }

    sealed class Action : PresenterAction {
        object ActionButtonClicked : Action()
    }
}
