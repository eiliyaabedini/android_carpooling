package com.deftmove.profile.profile

import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isGone
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.deftmove.carpooling.commonui.extension.getGenderLabelText
import com.deftmove.carpooling.interfaces.OpenEditProfileScreen
import com.deftmove.carpooling.interfaces.authentication.model.UserModel
import com.deftmove.heart.common.extension.toShortFormatString
import com.deftmove.heart.common.ui.ActivityWithPresenter
import com.deftmove.heart.common.ui.ScreenBucket
import com.deftmove.heart.common.ui.extension.bindView
import com.deftmove.heart.common.ui.extension.showAvatar
import com.deftmove.heart.interfaces.common.ui.ScreenBucketModel
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import com.deftmove.profile.R
import jp.wasabeef.glide.transformations.BlurTransformation
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.currentScope

class ProfileActivity : ActivityWithPresenter() {

    override fun getCurrentScreenBucket(): ScreenBucket<*> {
        return object : ScreenBucket<ProfilePresenter.View>(
              ScreenBucketModel(
                    scope = currentScope,
                    viewLayout = R.layout.activity_profile,
                    enableDisplayHomeAsUp = true
              )
        ) {
            override fun getPresenterView(): ProfilePresenter.View {
                return object : ProfilePresenter.View {
                    override fun showUserData(userModel: UserModel, isCompleted: Boolean) {
                        runOnUiThread {
                            viewUnComplete.isGone = isCompleted

                            imgProfile.showAvatar(userModel.avatarUrl, R.drawable.profile_screen_default_avatar)

                            Glide.with(baseContext)
                                  .load(userModel.avatarUrl)
                                  .apply(bitmapTransform(BlurTransformation()))
                                  .placeholder(R.drawable.layer_alpha_primary_background)
                                  .into(backgroundImageProfile)

                            txtName.text = String.format("%s %s", userModel.firstName, userModel.lastName)
                            txtPassengerRides.text = userModel.numberOfRidesAsPassenger.toString()
                            txtDriverRides.text = userModel.numberOfRidesAsDriver.toString()
                            txtAboutMe.text = userModel.aboutMe
                            txtAboutMe.gravity = if (userModel.aboutMe.isNullOrEmpty()) Gravity.END else Gravity.START
                            txtAboutMeTitle.isGone = !userModel.aboutMe.isNullOrEmpty()
                            txtPhoneNumber.text = userModel.phoneNumber
                            userModel.gender?.let { gender -> txtGender.text = gender.getGenderLabelText(textUtils) }
                            txtCarModel.text = userModel.carModel
                            txtCarNumber.text = userModel.carLicensePlate
                            txtEmail.text = userModel.email
                            txtMemberSince.text = textUtils.getString(
                                  R.string.activity_profile_member_since,
                                  userModel.memberSince.toShortFormatString()
                            )
                        }
                    }
                }
            }
        }
    }

    private val heartNavigator: HeartNavigator by inject()

    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val imgProfile: ImageView by bindView(R.id.profile_image_view)
    private val backgroundImageProfile: ImageView by bindView(R.id.profile_background_image_view)
    private val btnLogout: Button by bindView(R.id.profile_logout_button)
    private val viewUnComplete: View by bindView(R.id.profile_uncomplete_layout)
    private val btnUnComplete: Button by bindView(R.id.profile_uncomplete_button)
    private val txtName: TextView by bindView(R.id.profile_name_text)
    private val txtPassengerRides: TextView by bindView(R.id.profile_passenger_rides_text)
    private val txtDriverRides: TextView by bindView(R.id.profile_driver_rides_text)
    private val txtAboutMe: TextView by bindView(R.id.profile_short_bio_text)
    private val txtAboutMeTitle: View by bindView(R.id.profile_short_bio_title)
    private val txtPhoneNumber: TextView by bindView(R.id.profile_phone_number_text)
    private val txtGender: TextView by bindView(R.id.profile_gender_text)
    private val txtCarModel: TextView by bindView(R.id.profile_car_model_text)
    private val txtCarNumber: TextView by bindView(R.id.profile_car_number_text)
    private val txtEmail: TextView by bindView(R.id.profile_email_text)
    private val txtMemberSince: TextView by bindView(R.id.profile_member_since_text)

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile_action_edit -> {
                heartNavigator.getLauncher(OpenEditProfileScreen)
                      ?.startActivity()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun initializeViewListeners() {
        btnLogout.setOnClickListener {
            actions.onNext(ProfilePresenter.Action.LogoutClicked)
        }

        btnUnComplete.setOnClickListener {
            actions.onNext(ProfilePresenter.Action.UnCompleteProfileClicked)
        }
    }

    override fun getViewToolbar(): Toolbar? = toolbar
}
