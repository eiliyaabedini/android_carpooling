package com.deftmove.services.user

import com.deftmove.carpooling.interfaces.OpenSignInScreen
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.heart.common.event.DataEvent
import com.deftmove.heart.common.event.EventManager
import com.deftmove.heart.interfaces.navigator.HeartNavigator

class AuthenticationAssurance(
    private val eventManager: EventManager,
    private val currentUserManager: CurrentUserManager,
    private val heartNavigator: HeartNavigator
) {

    init {
        eventManager.observe()
              .ofType(DataEvent.NotAuthenticated::class.java)
              .doOnNext {
                  currentUserManager.clearCurrentUser()
                  heartNavigator.getLauncher(OpenSignInScreen)?.startActivity()
              }
              .subscribe()

    }
}
