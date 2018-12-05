package com.deftmove.profile.edit

import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.core.widget.NestedScrollView
import com.deftmove.carpooling.commonui.extension.getGenderLabelText
import com.deftmove.carpooling.interfaces.authentication.model.UserGender
import com.deftmove.heart.common.ui.ActivityWithPresenter
import com.deftmove.heart.common.ui.ScreenBucket
import com.deftmove.heart.common.ui.extension.bindView
import com.deftmove.heart.common.ui.extension.showAvatar
import com.deftmove.heart.common.ui.imagepicker.ImagePickerHandler
import com.deftmove.heart.interfaces.common.rx.doOnData
import com.deftmove.heart.interfaces.common.ui.ScreenBucketModel
import com.deftmove.profile.R
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.rxkotlin.plusAssign
import org.koin.androidx.scope.currentScope

class ProfileEditActivity : ActivityWithPresenter() {
    override fun getCurrentScreenBucket(): ScreenBucket<*> {
        return object : ScreenBucket<ProfileEditPresenter.View>(
              ScreenBucketModel(
                    scope = currentScope,
                    viewLayout = R.layout.activity_profile_edit,
                    enableDisplayHomeAsUp = true
              )
        ) {
            override fun getPresenterView(): ProfileEditPresenter.View {
                return object : ProfileEditPresenter.View {
                    override fun showUserData(model: ProfileEditPresenter.ProfileEditPresenterModel) {
                        runOnUiThread {
                            avatarImagegView.showAvatar(model.avatarUrl)

                            txtFirstName.setTextIfPossible(model.firstName)
                            txtLastName.setTextIfPossible(model.lastName)
                            model.gender?.let { rdoGender.setTextIfPossible(it.getGenderLabelText(textUtils)) }
                            txtPhoneNumber.setTextIfPossible(model.phoneNumber)
                            txtShortBio.setTextIfPossible(model.aboutMe)
                            txtEmail.setTextIfPossible(model.email)
                            txtCarModel.setTextIfPossible(model.carModel)
                            txtCarNumberPlate.setTextIfPossible(model.carLicensePlate)
                        }
                    }

                    override fun showLoadingInMenu() {
                        isLoading = true
                        runOnUiThread {
                            invalidateOptionsMenu()

                            saveOverlay.isGone = false
                        }
                    }

                    override fun showSaveInMenu() {
                        isLoading = false
                        runOnUiThread {
                            invalidateOptionsMenu()

                            saveOverlay.isGone = true
                        }
                    }

                    override fun showImagePicker() {
                        runOnUiThread {
                            imagePickerHandler.showItemsDialog(this@ProfileEditActivity, dialogFactory)
                        }
                    }

                    override fun showImagePickerLoading() {
                        runOnUiThread {
                            imagePickerIcon.isGone = true
                            imagePickerProgress.isGone = false
                        }
                    }

                    override fun hideImagePickerLoading() {
                        runOnUiThread {
                            imagePickerIcon.isGone = false
                            imagePickerProgress.isGone = true
                        }
                    }

                    override fun showGendersDialog() {
                        runOnUiThread {
                            dialogFactory.makeRadioItemsDialog(
                                  this@ProfileEditActivity,
                                  UserGender.MALE.getGenderLabelText(textUtils) to {
                                      rdoGender.setText(
                                            UserGender.MALE.getGenderLabelText(
                                                  textUtils
                                            )
                                      )
                                  },
                                  UserGender.FEMALE.getGenderLabelText(textUtils) to {
                                      rdoGender.setText(
                                            UserGender.FEMALE.getGenderLabelText(
                                                  textUtils
                                            )
                                      )
                                  },
                                  UserGender.UNKNOWN.getGenderLabelText(textUtils) to {
                                      rdoGender.setText(
                                            UserGender.UNKNOWN.getGenderLabelText(
                                                  textUtils
                                            )
                                      )
                                  }
                            )
                        }
                    }

                    override fun getFilledData(): ProfileEditPresenter.ProfileEditPresenterModel {
                        return ProfileEditPresenter.ProfileEditPresenterModel(
                              firstName = txtFirstName.getText(),
                              lastName = txtLastName.getText(),
                              gender = getSelectedGender(),
                              phoneNumber = txtPhoneNumber.getText(),
                              aboutMe = txtShortBio.getText(),
                              carModel = txtCarModel.getText(),
                              carLicensePlate = txtCarNumberPlate.getText()
                        )
                    }

                    override fun showFirstNameRequiredError() {
                        runOnUiThread {
                            txtFirstName.setError("FirstName is required")
                        }
                    }

                    override fun showLastNameRequiredError() {
                        runOnUiThread {
                            txtLastName.setError("LastName is required")
                        }
                    }

                    override fun showGenderRequiredError() {
                        runOnUiThread {
                            rdoGender.setError("Gender is required")
                        }
                    }
                }
            }
        }
    }

    private var isLoading: Boolean = false

    private val scrollViewRoot: NestedScrollView by bindView(R.id.profile_edit_scrollview_root)
    private val imagePickerLayout: View by bindView(R.id.profile_edit_avatar_image_layout)
    private val imagePickerIcon: View by bindView(R.id.profile_edit_avatar_image_icon)
    private val imagePickerProgress: View by bindView(R.id.profile_edit_avatar_image_progress)
    private val avatarImagegView: ImageView by bindView(R.id.profile_edit_avatar_image)
    private val txtFirstName: com.deftmove.carpooling.commonui.ui.CustomTextInputLayout by bindView(R.id.profile_edit_firstname_text)
    private val txtLastName: com.deftmove.carpooling.commonui.ui.CustomTextInputLayout by bindView(R.id.profile_edit_lastname_text)
    private val txtShortBio: com.deftmove.carpooling.commonui.ui.CustomTextInputLayout by bindView(R.id.profile_edit_short_bio_text)
    private val txtPhoneNumber: com.deftmove.carpooling.commonui.ui.CustomTextInputLayout by bindView(R.id.profile_edit_phone_number_text)
    private val txtEmail: com.deftmove.carpooling.commonui.ui.CustomTextInputLayout by bindView(R.id.profile_edit_email_text)
    private val rdoGenderLayout: View by bindView(R.id.profile_edit_gender_layout)
    private val rdoGender: com.deftmove.carpooling.commonui.ui.CustomTextInputLayout by bindView(R.id.profile_edit_gender_text)
    private val txtCarModel: com.deftmove.carpooling.commonui.ui.CustomTextInputLayout by bindView(R.id.profile_edit_car_model_text)
    private val txtCarNumberPlate: com.deftmove.carpooling.commonui.ui.CustomTextInputLayout
          by bindView(R.id.profile_edit_car_number_plate_text)
    private val saveOverlay: View by bindView(R.id.profile_edit_save_loading_overlay)

    private val imagePickerHandler: ImagePickerHandler =
          ImagePickerHandler()

    override fun initializeViewListeners() {

        enableSmartElevationForActionBar(scrollViewRoot)

        disposables += rdoGenderLayout.clicks()
              .subscribe {
                  actions.onNext(ProfileEditPresenter.Action.GenderViewClicked)
              }

        imagePickerLayout.setOnClickListener {
            actions.onNext(ProfileEditPresenter.Action.ImagePickerClicked)
        }

        disposables += imagePickerHandler.observe(this)
              .doOnData { actions.onNext(ProfileEditPresenter.Action.ImagePickerBitmapReceived(it)) }
              .subscribe()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_profile_edit, menu)

        menu.findItem(R.id.profile_edit_action_save).isVisible = !isLoading
        menu.findItem(R.id.profile_edit_action_loading).isVisible = isLoading

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile_edit_action_save -> {
                actions.onNext(ProfileEditPresenter.Action.SaveClicked)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getSelectedGender(): UserGender? {
        return when (rdoGender.getText()) {
            getString(R.string.common_gender_male_hint) -> UserGender.MALE
            getString(R.string.common_gender_female_hint) -> UserGender.FEMALE
            getString(R.string.common_gender_unknown_hint) -> UserGender.UNKNOWN
            else -> null
        }
    }
}
