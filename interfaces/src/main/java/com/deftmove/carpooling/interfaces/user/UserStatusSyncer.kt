package com.deftmove.carpooling.interfaces.user

interface UserStatusSyncer {

    //This function has to be called after when we store the user model into CurrentUserManager
    fun notifyUserLoggedIn()

    fun notifyAppOpened()

    fun notifyDeviceTokenChanged()

    fun notifyUserLoggedOut()
}
