package com.deftmove.splash

import android.app.NotificationManager
import android.content.Context
import com.deftmove.carpooling.interfaces.user.UserStatusSyncer
import com.deftmove.heart.common.ui.ActivityWithPresenter
import com.deftmove.heart.common.ui.ScreenBucket
import com.deftmove.heart.interfaces.common.ui.ScreenBucketModel
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.currentScope

class SplashActivity : ActivityWithPresenter() {

    private val userStatusSyncer: UserStatusSyncer by inject()

    override fun getCurrentScreenBucket(): ScreenBucket<*> {
        return object : ScreenBucket<SplashPresenter.View>(
              ScreenBucketModel(
                    scope = currentScope,
                    viewLayout = R.layout.activity_splash
              )
        ) {
            override fun getPresenterView(): SplashPresenter.View {
                return object : SplashPresenter.View {

                }
            }
        }
    }

    override fun initializeBeforePresenter() {
        userStatusSyncer.notifyAppOpened()

        //Cancel all visible notifications when user opens the APP
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
              .cancelAll()
    }
}
