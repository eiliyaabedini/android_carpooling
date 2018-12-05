package com.deftmove.services.workmanager

import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.deftmove.carpooling.interfaces.authentication.model.UserDeviceType
import com.deftmove.carpooling.interfaces.profile.service.ProfileService
import com.deftmove.heart.interfaces.common.firebase.messaging.FirebaseMessagingDelegate
import com.deftmove.heart.interfaces.common.rx.mapData
import com.deftmove.heart.interfaces.common.rx.toNotResult
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject

class UserStatusSyncerWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : RxWorker(appContext, workerParams), KoinComponent {

    private val firebaseMessagingDelegate: FirebaseMessagingDelegate by inject()
    private val profileService: ProfileService by inject()

    override fun createWork(): Single<Result> {

        return firebaseMessagingDelegate.getToken()
              .flatMapSingle { token ->
                  profileService.updateUserNoResponse(
                        deviceToken = token,
                        deviceType = UserDeviceType.ANDROID
                  )
                        .mapData { Result.success() }
                        .toNotResult()
                        .onErrorReturn { Result.failure() }
              }
    }
}
