package com.deftmove.authentication.register

import com.deftmove.authentication.R
import com.deftmove.carpooling.interfaces.OpenDebugAccountSwitcherScreen
import com.deftmove.heart.common.ui.ActivityWithPresenter
import com.deftmove.heart.common.ui.ScreenBucket
import com.deftmove.heart.common.ui.extension.bindView
import com.deftmove.heart.interfaces.common.ui.ScreenBucketModel
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.currentScope

class RegisterActivity : ActivityWithPresenter() {

    override fun getCurrentScreenBucket(): ScreenBucket<*> {
        return object : ScreenBucket<RegisterPresenter.View>(
              ScreenBucketModel(
                    scope = currentScope,
                    viewLayout = R.layout.activity_register
              )
        ) {
            override fun getPresenterView(): RegisterPresenter.View {
                return object : RegisterPresenter.View {

                    override fun showEmailError() {
                        runOnUiThread {
                            txtEmailAddress.setError(getString(R.string.register_email_address_error))
                        }
                    }
                }
            }
        }
    }

    private val txtEmailAddress: com.deftmove.carpooling.commonui.ui.CustomTextInputLayout by bindView(R.id.register_email_address)
    private val btnNext: FloatingActionButton by bindView(R.id.register_next_button)

    private val heartNavigator: HeartNavigator by inject()

    override fun initializeViewListeners() {
        btnNext.setOnClickListener {
            actions.onNext(
                  RegisterPresenter.Action.NextClicked(
                        emailAddress = txtEmailAddress.getText()
                  )
            )
        }

        btnNext.setOnLongClickListener {
            heartNavigator.getLauncher(OpenDebugAccountSwitcherScreen)
                  ?.startActivity()
            true
        }
    }
}
