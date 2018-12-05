package com.deftmove.carpooling.interfaces.authentication.network

import com.deftmove.heart.interfaces.common.Optional
import io.reactivex.Single

interface AuthenticationManager {

    fun getAuthToken(): String?

    fun getAuthTokenSingle(): Single<Optional<String>>
}
