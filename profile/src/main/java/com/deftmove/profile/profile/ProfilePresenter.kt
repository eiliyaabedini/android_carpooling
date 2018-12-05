package com.deftmove.profile.profile

import com.deftmove.carpooling.interfaces.OpenEditProfileScreen
import com.deftmove.carpooling.interfaces.OpenSignInScreen
import com.deftmove.carpooling.interfaces.authentication.model.UserModel
import com.deftmove.carpooling.interfaces.authentication.services.AuthenticationService
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.carpooling.interfaces.user.UserStatusSyncer
import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonAction
import com.deftmove.heart.interfaces.common.presenter.PresenterView
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import timber.log.Timber

class ProfilePresenter(
    private val currentUserManager: CurrentUserManager,
    private val authenticationService: AuthenticationService,
    private val userStatusSyncer: UserStatusSyncer,
    private val heartNavigator: HeartNavigator,
    private val reactiveTransformer: ReactiveTransformer
) : Presenter<ProfilePresenter.View>() {

    override fun initialise() {

        commonView?.actions()
              ?.subscribeOn(reactiveTransformer.ioScheduler())
              ?.subscribeSafeWithShowingErrorContent { action ->
                  Timber.d("action: $action")
                  when (action) {
                      is PresenterCommonAction.ErrorRetryClicked -> {
                          commonView?.showContent()
                      }

                      is Action.LogoutClicked -> {
                          commonView?.showContentLoading()

                          if (currentUserManager.isFastLogin()) {
                              currentUserManager.clearCurrentUser()
                              userStatusSyncer.notifyUserLoggedOut()
                              heartNavigator.getLauncher(OpenSignInScreen)?.startActivity()
                          } else {
                              authenticationService.logout()
                                    .subscribeOn(reactiveTransformer.ioScheduler())
                                    .doOnSuccess {
                                        heartNavigator.getLauncher(OpenSignInScreen)?.startActivity()
                                    }
                                    .subscribeSafeResponseWithShowingErrorContent()
                          }
                      }

                      is Action.UnCompleteProfileClicked -> {
                          heartNavigator.getLauncher(OpenEditProfileScreen)?.startActivity()
                      }

                      is Action.EditProfileClicked -> {
                          heartNavigator.getLauncher(OpenEditProfileScreen)?.startActivity()
                      }
                  }
              }

        currentUserManager.changes()
              .subscribeOn(reactiveTransformer.ioScheduler())
              .subscribeSafeWithShowingErrorContent { currentUser ->
                  currentUser.user?.let { userModel ->
                      updateUserDataInView(userModel)
                  }
              }
    }

    private fun updateUserDataInView(userModel: UserModel) {
        view?.showUserData(userModel, isProfileCompleted(userModel))
    }

    private fun isProfileCompleted(userModel: UserModel): Boolean {
        return !userModel.avatarUrl.isNullOrEmpty() &&
              !userModel.aboutMe.isNullOrEmpty() &&
              !userModel.phoneNumber.isNullOrEmpty()
    }

    interface View : PresenterView {
        fun showUserData(userModel: UserModel, isCompleted: Boolean)
    }

    sealed class Action : PresenterAction {
        object UnCompleteProfileClicked : Action()
        object EditProfileClicked : Action()
        object LogoutClicked : Action()
    }
}
