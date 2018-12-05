package com.deftmove.carpooling.interfaces.service

import com.deftmove.carpooling.interfaces.authentication.model.UserGender
import com.deftmove.carpooling.interfaces.profile.model.UserProfileModel
import com.deftmove.heart.interfaces.ResponseResult
import io.reactivex.Observable

interface OnboardingService {

    fun onboardUser(
        firstname: String,
        lastname: String,
        gender: UserGender,
        aboutMe: String?,
        phoneNumber: String
    ): Observable<ResponseResult<UserProfileModel>>
}
