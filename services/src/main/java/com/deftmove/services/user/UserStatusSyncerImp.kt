package com.deftmove.services.user

import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.carpooling.interfaces.user.UserStatusSyncer
import com.deftmove.carpooling.interfaces.workmanager.WorkManagerFactory
import com.deftmove.heart.interfaces.koin.getAuthenticatedUserScope
import org.koin.core.KoinComponent

class UserStatusSyncerImp(
    private val currentUserManager: CurrentUserManager,
    private val workManagerFactory: WorkManagerFactory
) : UserStatusSyncer, KoinComponent {

    override fun notifyUserLoggedIn() {
        runUserStatusSyncer()
    }

    override fun notifyAppOpened() {
        runUserStatusSyncer()
    }

    override fun notifyDeviceTokenChanged() {
        runUserStatusSyncer()
    }

    override fun notifyUserLoggedOut() {
        getAuthenticatedUserScope().close()
    }

    private fun runUserStatusSyncer() {
        if (currentUserManager.isAuthenticated() && !currentUserManager.isFastLogin()) {
            workManagerFactory.runUserStatusSyncerWorker()
        }
    }
}
