package com.deftmove.services.profile

import io.reactivex.Single
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ProfileApi {

    @Multipart
    @POST("v1/upload_avatar")
    fun uploadAvatar(
        @Part file: MultipartBody.Part
    ): Single<Unit>
}
