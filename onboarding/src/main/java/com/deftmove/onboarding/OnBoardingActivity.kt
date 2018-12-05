package com.deftmove.onboarding

import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import com.deftmove.carpooling.commonui.extension.getGenderLabelText
import com.deftmove.carpooling.commonui.ui.CustomTextInputLayout
import com.deftmove.carpooling.interfaces.authentication.model.UserGender
import com.deftmove.heart.common.ui.ActivityWithPresenter
import com.deftmove.heart.common.ui.ScreenBucket
import com.deftmove.heart.common.ui.extension.bindView
import com.deftmove.heart.interfaces.common.ui.ScreenBucketModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.reactivex.rxkotlin.plusAssign
import org.koin.androidx.scope.currentScope

class OnBoardingActivity : ActivityWithPresenter() {

    override fun getCurrentScreenBucket(): ScreenBucket<*> {
        return object : ScreenBucket<OnBoardingPresenter.View>(
              ScreenBucketModel(
                    scope = currentScope,
                    viewLayout = R.layout.activity_onboarding,
                    enableDisplayHomeAsUp = true
              )
        ) {
            override fun getPresenterView(): OnBoardingPresenter.View {
                return object : OnBoardingPresenter.View {

                    override fun showGendersDialog() {
                        runOnUiThread {
                            dialogFactory.makeRadioItemsDialog(
                                  this@OnBoardingActivity,
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

                    override fun getFilledData(): OnBoardingPresenter.OnBoardingPresenterModel {
                        return OnBoardingPresenter.OnBoardingPresenterModel(
                              firstName = txtFirstName.getText(),
                              lastName = txtLastName.getText(),
                              gender = getSelectedGender(),
                              phoneNumber = txtPhoneNumber.getText()
                        )
                    }

                    override fun showFirstNameRequiredError() {
                        runOnUiThread {
                            txtFirstName.setError(getString(R.string.activity_first_name_required_error))
                        }
                    }

                    override fun showLastNameRequiredError() {
                        runOnUiThread {
                            txtLastName.setError(getString(R.string.activity_last_name_required_error))
                        }
                    }

                    override fun showGenderRequiredError() {
                        runOnUiThread {
                            rdoGender.setError(getString(R.string.activity_gender_required_error))
                        }
                    }

                    override fun showPhoneNumberRequiredError() {
                        runOnUiThread {
                            txtPhoneNumber.setError(getString(R.string.activity_phone_number_required_error))
                        }
                    }
                }
            }
        }
    }

    private val fieldsScrollView: NestedScrollView by bindView(R.id.onboarding_fields_scrollview)
    private val txtFirstName: CustomTextInputLayout by bindView(R.id.onboarding_firstname_text)
    private val txtLastName: CustomTextInputLayout by bindView(R.id.onboarding_lastname_text)
    private val rdoGender: CustomTextInputLayout by bindView(R.id.onboarding_gender_text)
    private val txtPhoneNumber: CustomTextInputLayout by bindView(R.id.onboarding_phonenummber_text)
    private val btnFab: FloatingActionButton by bindView(R.id.onboarding_next_fab)

    override fun initializeViewListeners() {

        enableSmartElevationForActionBar(fieldsScrollView)

        disposables += rdoGender.disabledClicks()
              .subscribe {
                  actions.onNext(OnBoardingPresenter.Action.GenderViewClicked)
              }

        btnFab.setOnClickListener {
            actions.onNext(
                  OnBoardingPresenter.Action.SubmitClicked
            )
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

    override fun getViewToolbar(): Toolbar? = null
}
