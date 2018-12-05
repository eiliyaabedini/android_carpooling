package com.deftmove.profile.edit

import com.deftmove.carpooling.interfaces.authentication.model.UserGender
import com.deftmove.carpooling.interfaces.authentication.model.UserModel
import com.deftmove.carpooling.interfaces.profile.service.ProfileService
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonAction
import com.deftmove.heart.interfaces.common.presenter.PresenterView
import com.deftmove.heart.interfaces.common.rx.doOnData
import com.deftmove.heart.interfaces.common.rx.doOnFailure
import timber.log.Timber
import java.io.File

class ProfileEditPresenter(
    private val profileService: ProfileService,
    private val currentUserManager: CurrentUserManager,
    private val reactiveTransformer: ReactiveTransformer
) : Presenter<ProfileEditPresenter.View>() {

    override fun initialise() {

        commonView?.actions()
              ?.subscribeOn(reactiveTransformer.ioScheduler())
              ?.subscribeSafeWithShowingErrorContent { action ->
                  Timber.d("action:%s", action)
                  when (action) {
                      is PresenterCommonAction.ErrorRetryClicked -> {
                          commonView?.showContent()
                      }

                      is Action.ImagePickerClicked -> {
                          view?.showImagePicker()
                      }

                      is Action.ImagePickerBitmapReceived -> {
                          uploadAvatarImage(action.imageFile)
                      }

                      is Action.GenderViewClicked -> {
                          view?.showGendersDialog()
                      }

                      Action.SaveClicked -> {
                          val model = view?.getFilledData()!!

                          if (validateUserAndShowErrors(model)) {
                              updateUser(model)
                          }
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

    //Returns a boolean to show if model was valid or not
    private fun validateUserAndShowErrors(model: ProfileEditPresenterModel): Boolean {
        var isValid = true

        if (model.firstName.isNullOrEmpty()) {
            isValid = false
            view?.showFirstNameRequiredError()
        }

        if (model.lastName.isNullOrEmpty()) {
            isValid = false
            view?.showLastNameRequiredError()
        }

        if (model.gender == null) {
            isValid = false
            view?.showGenderRequiredError()
        }

        if (model.aboutMe != null && model.aboutMe.length > MAX_ABOUT_ME_LENGTH) {
            isValid = false
            //View will show errors for that automatically when it is more than MAX_ABOUT_ME_LENGTH
        }

        return isValid
    }

    private fun updateUser(model: ProfileEditPresenterModel) {
        Timber.d("updateUser called")

        view?.showLoadingInMenu()

        profileService.updateUser(
              firstName = model.firstName,
              lastName = model.lastName,
              gender = model.gender,
              phoneNumber = model.phoneNumber,
              aboutMe = model.aboutMe,
              carModel = model.carModel,
              carLicensePlate = model.carLicensePlate
        )
              .subscribeOn(reactiveTransformer.ioScheduler())
              .doOnData {
                  commonView?.closeScreen()
              }
              .doOnFailure { view?.showSaveInMenu() }
              .subscribeSafeResponseWithShowingErrorContent()
    }

    private fun uploadAvatarImage(imageFile: File) {
        Timber.d("uploadAvatarImage called")

        view?.showImagePickerLoading()

        profileService.uploadAvatarImage(imageFile)
              .subscribeOn(reactiveTransformer.ioScheduler())
              .doOnData { view?.hideImagePickerLoading() }
              .doOnFailure { view?.hideImagePickerLoading() }
              .subscribeSafeResponseWithShowingErrorContent()
    }

    private fun updateUserDataInView(userModel: UserModel) {
        view?.showUserData(
              ProfileEditPresenterModel(
                    firstName = userModel.firstName,
                    lastName = userModel.lastName,
                    gender = userModel.gender,
                    phoneNumber = userModel.phoneNumber,
                    aboutMe = userModel.aboutMe,
                    email = userModel.email,
                    carModel = userModel.carModel,
                    carLicensePlate = userModel.carLicensePlate,
                    avatarUrl = userModel.avatarUrl
              )
        )
    }

    interface View : PresenterView {
        fun showUserData(model: ProfileEditPresenterModel)

        fun showLoadingInMenu()
        fun showSaveInMenu()

        fun showImagePicker()
        fun showImagePickerLoading()
        fun hideImagePickerLoading()

        fun showGendersDialog()
        fun getFilledData(): ProfileEditPresenterModel

        fun showFirstNameRequiredError()
        fun showLastNameRequiredError()
        fun showGenderRequiredError()
    }

    sealed class Action : PresenterAction {
        object ImagePickerClicked : Action()
        data class ImagePickerBitmapReceived(val imageFile: File) : Action()
        object GenderViewClicked : Action()
        object SaveClicked : Action()
    }

    data class ProfileEditPresenterModel(
        val firstName: String?,
        val lastName: String?,
        val gender: UserGender?,
        val phoneNumber: String?,
        val aboutMe: String?,
        val carModel: String?,
        val carLicensePlate: String?,
        val email: String? = null,
        val avatarUrl: String? = null
    )

    companion object {
        private const val MAX_ABOUT_ME_LENGTH: Int = 250
    }
}
