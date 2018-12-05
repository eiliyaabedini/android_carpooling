package com.deftmove.services.network

import com.deftmove.carpooling.interfaces.authentication.network.AuthenticationManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthenticationInterceptor(
    private val authenticationManager: AuthenticationManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()

        authenticationManager.getAuthToken()?.let {
            builder.addHeader(AUTHENTICATION_HEADER_KEY, "Bearer $it")
        }

        return chain.proceed(builder.build())
    }

    companion object {
        const val AUTHENTICATION_HEADER_KEY = "Authorization"
    }
}
