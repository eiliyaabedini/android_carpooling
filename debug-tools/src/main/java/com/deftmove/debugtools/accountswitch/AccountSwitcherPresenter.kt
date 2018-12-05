package com.deftmove.debugtools.accountswitch

import com.deftmove.carpooling.interfaces.authentication.services.AuthenticationService
import com.deftmove.carpooling.interfaces.profile.service.ProfileService
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.debugtools.DebugConsts
import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonAction
import com.deftmove.heart.interfaces.common.presenter.PresenterView
import com.deftmove.heart.interfaces.common.rx.doOnData
import com.deftmove.heart.interfaces.common.rx.flatMapData
import timber.log.Timber
import java.util.concurrent.TimeUnit

class AccountSwitcherPresenter(
    private val currentUserManager: CurrentUserManager,
    private val profileService: ProfileService,
    private val authenticationService: AuthenticationService,
    private val reactiveTransformer: ReactiveTransformer
) : Presenter<AccountSwitcherPresenter.View>() {

    private val accounts: List<Pair<String, String>> = DebugConsts.accounts.toList().toMutableList()

    override fun initialise() {
        commonView?.actions()
              ?.subscribeOn(reactiveTransformer.ioScheduler())
              ?.subscribeSafeWithShowingErrorContent { action ->
                  Timber.d("action:%s", action)
                  when (action) {
                      is PresenterCommonAction.ErrorRetryClicked -> {
                          commonView?.showContent()
                      }

                      is Action.AccountSelected -> {
                          val account = accounts[action.index]

                          currentUserManager.setIsFastLogin(action.isFastLogin)
                          currentUserManager.setApiToke(account.second)

                          profileService.getCurrentUser()
                                .doOnSubscribe { commonView?.showContentLoading() }
                                .flatMapData { authenticationService.getCurrentCustomer() }
                                .subscribeOn(reactiveTransformer.ioScheduler())
                                .delay(DELAY_BEFORE_SWITCHING_TO_NEW_USER, TimeUnit.MILLISECONDS)
                                .doOnData {
                                    commonView?.restartAPP()
                                }
                                .subscribeSafeResponseWithShowingErrorContent()
                      }
                  }
              }

        view?.updateAccountsList(accounts.map { it.first })
    }

    interface View : PresenterView {
        fun updateAccountsList(accounts: List<String>)
    }

    sealed class Action : PresenterAction {
        data class AccountSelected(val index: Int, val isFastLogin: Boolean) : Action()
    }

    companion object {
        private const val DELAY_BEFORE_SWITCHING_TO_NEW_USER: Long = 5000
    }
}
