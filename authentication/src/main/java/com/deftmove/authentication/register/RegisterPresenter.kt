package com.deftmove.authentication.register

import com.deftmove.authentication.firebase.FirebaseLinkCreator
import com.deftmove.carpooling.interfaces.OpenAuthenticationMagicTokenSentDialog
import com.deftmove.carpooling.interfaces.authentication.services.AuthenticationService
import com.deftmove.heart.common.extension.isEmailValid
import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonAction
import com.deftmove.heart.interfaces.common.presenter.PresenterView
import com.deftmove.heart.interfaces.common.rx.doOnData
import com.deftmove.heart.interfaces.common.rx.flatMapData
import com.deftmove.heart.interfaces.common.rx.toResult
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import timber.log.Timber

class RegisterPresenter(
    private val reactiveTransformer: ReactiveTransformer,
    private val authenticationService: AuthenticationService,
    private val firebaseLinkCreator: FirebaseLinkCreator,
    private val heartNavigator: HeartNavigator
) : Presenter<RegisterPresenter.View>() {

    override fun initialise() {

        commonView?.actions()
              ?.subscribeSafeWithShowingErrorContent { action ->
                  Timber.d("action: $action")
                  when (action) {
                      is PresenterCommonAction.ErrorRetryClicked -> {
                          commonView?.showContent()
                      }

                      is Action.NextClicked -> {
                          if (isEmailValid(action.emailAddress)) {
                              createToken(action)
                          } else {
                              view?.showEmailError()
                          }
                      }
                  }
              }
    }

    private fun createToken(action: Action.NextClicked) {
        Timber.d("createToken called: $action")

        commonView?.showContentLoading()

        authenticationService.createMagicToken(action.emailAddress)
              .subscribeOn(reactiveTransformer.ioScheduler())
              .flatMapData { magicToken ->
                  firebaseLinkCreator.createLink(magicToken).map { it to magicToken }.toObservable().toResult()
              }
              .flatMapData { (magicLink, magicToken) ->
                  authenticationService.putMagicTokenLink(magicLink, magicToken)
              }
              .doOnData { result ->
                  if (result) {
                      heartNavigator.getLauncher(
                            OpenAuthenticationMagicTokenSentDialog(action.emailAddress)
                      )?.startActivity()
                      commonView?.showContent()
                  } else {
                      commonView?.showContentError()
                  }
              }
              .subscribeSafeResponseWithShowingErrorContent()
    }

    interface View : PresenterView {
        fun showEmailError()
    }

    sealed class Action : PresenterAction {
        data class NextClicked(val emailAddress: String) : Action()
    }
}
