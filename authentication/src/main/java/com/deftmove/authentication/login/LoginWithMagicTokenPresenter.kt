package com.deftmove.authentication.login

import com.deftmove.carpooling.interfaces.OpenRideFeedOrRegistrationScreen
import com.deftmove.carpooling.interfaces.OpenSignInScreen
import com.deftmove.carpooling.interfaces.authentication.services.AuthenticationService
import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonAction
import com.deftmove.heart.interfaces.common.presenter.PresenterView
import com.deftmove.heart.interfaces.common.rx.doOnData
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import timber.log.Timber

class LoginWithMagicTokenPresenter(
    private val reactiveTransformer: ReactiveTransformer,
    private val authenticationService: AuthenticationService,
    private val heartNavigator: HeartNavigator
) : Presenter<LoginWithMagicTokenPresenter.View>() {

    override fun initialise() {
        commonView?.showContentLoading()

        commonView?.actions()
              ?.subscribeOn(reactiveTransformer.ioScheduler())
              ?.subscribeSafeWithShowingErrorContent { action ->
                  Timber.d("action:%s", action)
                  when (action) {
                      is PresenterCommonAction.ErrorRetryClicked -> {
                          heartNavigator.getLauncher(OpenSignInScreen)
                                ?.startActivity()
                      }
                  }
              }

        authenticationService.loginWithMagicToken(view?.getReceivedToken()!!)
              .subscribeOn(reactiveTransformer.ioScheduler())
              .doOnData {
                  heartNavigator.getLauncher(OpenRideFeedOrRegistrationScreen)
                        ?.startActivity()
              }
              .doFinally {
                  commonView?.closeScreen()
              }
              .subscribeSafeResponseWithShowingErrorContent()
    }

    interface View : PresenterView {
        fun getReceivedToken(): String
    }

    sealed class Action : PresenterAction
}
