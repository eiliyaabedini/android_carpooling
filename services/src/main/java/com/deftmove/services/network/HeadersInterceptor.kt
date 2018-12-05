package com.deftmove.services.network

import com.deftmove.services.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class HeadersInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()

        builder.addHeader(X_DEVICE_TYPE, DEVICE_TYPE)
        builder.addHeader(X_APP_VERSION, BuildConfig.CONFIG_APP_VERSION_NAME)
        builder.addHeader(CUSTOMER_IDENTIFIER, BuildConfig.CONFIG_CUSTOMER_IDENTIFIER)

        return chain.proceed(builder.build())
    }

    companion object {
        const val X_DEVICE_TYPE = "DEVICE-TYPE"
        const val X_APP_VERSION = "APP-VERSION"
        const val DEVICE_TYPE = "Android"
        const val CUSTOMER_IDENTIFIER = "customer-identifier"
    }
}
