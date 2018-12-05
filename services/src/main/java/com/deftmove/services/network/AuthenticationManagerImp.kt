package com.deftmove.services.network

import com.deftmove.carpooling.interfaces.authentication.network.AuthenticationManager
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.heart.interfaces.common.Optional
import com.deftmove.heart.interfaces.common.getOrNull
import com.deftmove.heart.interfaces.common.toOptional
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class AuthenticationManagerImp(
    private val currentUserManager: CurrentUserManager
) : AuthenticationManager {

    override fun getAuthToken(): String? = getAuthTokenSingle().blockingGet().getOrNull()

    override fun getAuthTokenSingle(): Single<Optional<String>> {
        return Single.create<Optional<String>> { emitter ->
            emitter.onSuccess(currentUserManager.getApiToken().toOptional())
        }.subscribeOn(Schedulers.io())
    }
}
