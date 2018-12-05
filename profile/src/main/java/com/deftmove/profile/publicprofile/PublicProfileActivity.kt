package com.deftmove.profile.publicprofile

import android.view.Gravity
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isGone
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.deftmove.carpooling.commonui.extension.getGenderLabelText
import com.deftmove.carpooling.commonui.ui.VerticalIcons
import com.deftmove.carpooling.interfaces.authentication.model.UserModel
import com.deftmove.carpooling.interfaces.common.data.IconsType
import com.deftmove.heart.common.extension.toShortFormatString
import com.deftmove.heart.common.ui.ActivityWithPresenter
import com.deftmove.heart.common.ui.ScreenBucket
import com.deftmove.heart.common.ui.extension.bindView
import com.deftmove.heart.common.ui.extension.showAvatar
import com.deftmove.heart.interfaces.common.ui.ScreenBucketModel
import com.deftmove.heart.interfaces.navigator.retrieveArgument
import com.deftmove.profile.R
import jp.wasabeef.glide.transformations.BlurTransformation
import org.koin.androidx.scope.currentScope
import timber.log.Timber

class PublicProfileActivity : ActivityWithPresenter() {

    override fun getCurrentScreenBucket(): ScreenBucket<*> {
        return object : ScreenBucket<PublicProfilePresenter.View>(
              ScreenBucketModel(
                    scope = currentScope,
                    viewLayout = R.layout.activity_public_profile,
                    enableDisplayHomeAsUp = true
              )
        ) {
            override fun getPresenterView(): PublicProfilePresenter.View {
                return object : PublicProfilePresenter.View {

                    override fun getReceivedUserId(): String {
                        return receivedUserId
                    }

                    override fun showUserData(userModel: UserModel, isCompleted: Boolean) {
                        runOnUiThread {
                            imgProfile.showAvatar(userModel.avatarUrl)

                            Glide.with(baseContext)
                                  .load(userModel.avatarUrl)
                                  .apply(bitmapTransform(BlurTransformation()))
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

    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val imgProfile: ImageView by bindView(R.id.public_profile_image_view)
    private val backgroundImageProfile: ImageView by bindView(R.id.public_profile_background_image_view)
    private val txtName: TextView by bindView(R.id.public_profile_name_text)
    private val txtPassengerRides: TextView by bindView(R.id.public_profile_passenger_rides_text)
    private val txtDriverRides: TextView by bindView(R.id.public_profile_driver_rides_text)
    private val txtAboutMe: TextView by bindView(R.id.public_profile_short_bio_text)
    private val txtAboutMeTitle: TextView by bindView(R.id.public_profile_short_bio_title)
    private val txtPhoneNumber: TextView by bindView(R.id.public_profile_phone_number_text)
    private val txtGender: TextView by bindView(R.id.public_profile_gender_text)
    private val txtCarModel: TextView by bindView(R.id.public_profile_car_model_text)
    private val txtCarNumber: TextView by bindView(R.id.public_profile_car_number_text)
    private val txtMemberSince: TextView by bindView(R.id.public_profile_member_since_text)
    private val actionIcons: VerticalIcons by bindView(R.id.public_profile_action_icons)

    private lateinit var receivedUserId: String

    override fun initializeBeforePresenter() {
        receivedUserId = retrieveArgument()!!
        Timber.d("receivedUserId:$receivedUserId")
    }

    override fun initializeViewListeners() {
        actionIcons.bind(type = IconsType.ProfilePublicScreenActionBar(receivedUserId))
    }

    override fun getViewToolbar(): Toolbar? = toolbar
}
