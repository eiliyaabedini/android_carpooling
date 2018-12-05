package com.deftmove.services.workmanager

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.deftmove.carpooling.interfaces.workmanager.WorkManagerFactory

class WorkManagerFactoryImp : WorkManagerFactory {

    override fun runUserStatusSyncerWorker() {
        val userStatusSyncerWorker = OneTimeWorkRequest.Builder(UserStatusSyncerWorker::class.java)
              .setConstraints(
                    Constraints.Builder()
                          .setRequiredNetworkType(NetworkType.CONNECTED)
                          .build()
              )
              .build()

        WorkManager.getInstance().enqueue(userStatusSyncerWorker)
    }
}
