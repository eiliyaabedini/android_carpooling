package com.deftmove.authentication.login

import com.deftmove.authentication.R
import com.deftmove.carpooling.interfaces.authentication.login.LoginWithMagicTokenModel
import com.deftmove.heart.common.ui.ActivityWithPresenter
import com.deftmove.heart.common.ui.ScreenBucket
import com.deftmove.heart.interfaces.common.ui.ScreenBucketModel
import com.deftmove.heart.interfaces.navigator.retrieveArgument
import org.koin.androidx.scope.currentScope
import timber.log.Timber

class LoginWithMagicTokenActivity : ActivityWithPresenter() {

    override fun getCurrentScreenBucket(): ScreenBucket<*> {
        return object : ScreenBucket<LoginWithMagicTokenPresenter.View>(
              ScreenBucketModel(
                    scope = currentScope,
                    viewLayout = R.layout.activity_login_with_magic_token
              )
        ) {
            override fun getPresenterView(): LoginWithMagicTokenPresenter.View {
                return object : LoginWithMagicTokenPresenter.View {
                    override fun getReceivedToken(): String = loginWithMagicTokenModelArgument!!.magicToken
                }
            }
        }
    }

    private var loginWithMagicTokenModelArgument: LoginWithMagicTokenModel? = null

    override fun initializeBeforePresenter() {
        loginWithMagicTokenModelArgument = retrieveArgument()
        Timber.d("loginWithMagicTokenModelArgument:${loginWithMagicTokenModelArgument.toString()}")
    }
}
