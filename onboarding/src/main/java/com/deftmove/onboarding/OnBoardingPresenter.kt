package com.deftmove.onboarding

import com.deftmove.carpooling.interfaces.OpenRideFeedOrRegistrationScreen
import com.deftmove.carpooling.interfaces.authentication.model.UserGender
import com.deftmove.carpooling.interfaces.service.OnboardingService
import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonAction
import com.deftmove.heart.interfaces.common.presenter.PresenterView
import com.deftmove.heart.interfaces.common.rx.doOnData
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import timber.log.Timber

class OnBoardingPresenter(
    private val reactiveTransformer: ReactiveTransformer,
    private val onboardingService: OnboardingService,
    private val heartNavigator: HeartNavigator
) : Presenter<OnBoardingPresenter.View>() {

    override fun initialise() {
        commonView?.actions()
              ?.subscribeSafeWithShowingErrorContent { action ->
                  Timber.d("action: $action")
                  when (action) {
                      is PresenterCommonAction.ErrorRetryClicked -> {
                          commonView?.showContent()
                      }

                      is Action.SubmitClicked -> {
                          val model = view?.getFilledData()

                          if (model != null && validateUserAndShowErrors(model)) {
                              onboardUser(model)
                          }
                      }

                      is Action.GenderViewClicked -> {
                          view?.showGendersDialog()
                      }
                  }
              }
    }

    //Returns a boolean to show if model was valid or not
    private fun validateUserAndShowErrors(model: OnBoardingPresenterModel): Boolean {
        var isValid = true

        if (model.firstName.isEmpty()) {
            isValid = false
            view?.showFirstNameRequiredError()
        }

        if (model.lastName.isEmpty()) {
            isValid = false
            view?.showLastNameRequiredError()
        }

        if (model.gender == null) {
            isValid = false
            view?.showGenderRequiredError()
        }

        if (model.phoneNumber.isEmpty()) {
            isValid = false
            view?.showPhoneNumberRequiredError()
        }

        return isValid
    }

    private fun onboardUser(model: OnBoardingPresenterModel) {
        commonView?.showContentLoading()

        onboardingService.onboardUser(
              firstname = model.firstName,
              lastname = model.lastName,
              gender = model.gender!!,
              phoneNumber = model.phoneNumber,
              aboutMe = null
        )
              .subscribeOn(reactiveTransformer.ioScheduler())
              .doOnData {
                  heartNavigator.getLauncher(OpenRideFeedOrRegistrationScreen)
                        ?.startActivity()
              }
              .subscribeSafeResponseWithShowingErrorContent()
    }

    interface View : PresenterView {
        fun showGendersDialog()
        fun getFilledData(): OnBoardingPresenterModel

        fun showFirstNameRequiredError()
        fun showLastNameRequiredError()
        fun showGenderRequiredError()
        fun showPhoneNumberRequiredError()
    }

    sealed class Action : PresenterAction {
        object SubmitClicked : Action()
        object GenderViewClicked : Action()
    }

    data class OnBoardingPresenterModel(
        val firstName: String,
        val lastName: String,
        val gender: UserGender?,
        val phoneNumber: String
    )
}
