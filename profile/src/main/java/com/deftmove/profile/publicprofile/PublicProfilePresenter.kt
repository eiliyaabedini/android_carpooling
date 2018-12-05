package com.deftmove.profile.publicprofile

import com.deftmove.carpooling.interfaces.authentication.model.UserModel
import com.deftmove.carpooling.interfaces.profile.service.ProfileService
import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonAction
import com.deftmove.heart.interfaces.common.presenter.PresenterView
import com.deftmove.heart.interfaces.common.rx.mapData
import timber.log.Timber

class PublicProfilePresenter(
    private val profileService: ProfileService,
    private val reactiveTransformer: ReactiveTransformer
) : Presenter<PublicProfilePresenter.View>() {

    override fun initialise() {

        commonView?.actions()
              ?.subscribeOn(reactiveTransformer.ioScheduler())
              ?.subscribeSafeWithShowingErrorContent { action ->
                  Timber.d("action: $action")
                  when (action) {
                      is PresenterCommonAction.ErrorRetryClicked -> {
                          commonView?.showContent()
                      }
                  }
              }

        profileService.findUser(view?.getReceivedUserId()!!)
              .doOnSubscribe { commonView?.showContentLoading() }
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData { user ->
                  updateUserDataInView(user)
              }
              .subscribeSafeResponseWithShowingErrorContent()
    }

    private fun updateUserDataInView(userModel: UserModel) {
        commonView?.showContent()
        view?.showUserData(userModel, isProfileCompleted(userModel))
    }

    private fun isProfileCompleted(userModel: UserModel): Boolean {
        return !userModel.avatarUrl.isNullOrEmpty() &&
              !userModel.aboutMe.isNullOrEmpty() &&
              !userModel.phoneNumber.isNullOrEmpty()
    }

    interface View : PresenterView {
        fun getReceivedUserId(): String
        fun showUserData(userModel: UserModel, isCompleted: Boolean)
    }
}
