package com.deftmove.splash

import com.deftmove.carpooling.interfaces.OpenRideFeedOrRegistrationScreen
import com.deftmove.carpooling.interfaces.OpenSignInScreen
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonAction
import com.deftmove.heart.interfaces.common.presenter.PresenterView
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import io.reactivex.Single
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SplashPresenter(
    private val userManager: CurrentUserManager,
    private val heartNavigator: HeartNavigator,
    private val reactiveTransformer: ReactiveTransformer
) : Presenter<SplashPresenter.View>() {

    override fun initialise() {

        commonView?.actions()
              ?.subscribeSafeWithShowingErrorContent { action ->
                  Timber.d("action: $action")
                  when (action) {
                      is PresenterCommonAction.ErrorRetryClicked -> {
                          heartNavigator.getLauncher(OpenSignInScreen)?.startActivity()
                      }
                  }
              }

        Single.just(Unit)
              .subscribeOn(reactiveTransformer.ioScheduler())
              .delay(SPLASH_SCREEN_DELAY, TimeUnit.MILLISECONDS)
              .doOnSuccess {

                  if (userManager.isAuthenticated()) {
                      heartNavigator.getLauncher(
                            OpenRideFeedOrRegistrationScreen
                      )?.startActivity()
                  } else {
                      heartNavigator.getLauncher(OpenSignInScreen)?.startActivity()
                  }

                  commonView?.closeScreen()
              }
              .subscribeSafeWithShowingErrorContent()
    }

    interface View : PresenterView

    interface Action : PresenterAction

    companion object {
        private const val SPLASH_SCREEN_DELAY: Long = 2500
    }
}
