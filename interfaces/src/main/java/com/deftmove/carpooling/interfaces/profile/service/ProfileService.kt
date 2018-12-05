package com.deftmove.carpooling.interfaces.profile.service

import com.deftmove.carpooling.interfaces.authentication.model.UserDeviceType
import com.deftmove.carpooling.interfaces.authentication.model.UserGender
import com.deftmove.carpooling.interfaces.authentication.model.UserModel
import com.deftmove.carpooling.interfaces.profile.model.UserProfileModel
import com.deftmove.heart.interfaces.ResponseResult
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File

interface ProfileService {

    fun findUser(userId: String): Observable<ResponseResult<UserModel>>

    fun getCurrentUser(): Single<ResponseResult<UserModel>>

    fun updateUser(
        aboutMe: String? = null,
        carLicensePlate: String? = null,
        carModel: String? = null,
        deviceToken: String? = null,
        deviceType: UserDeviceType? = null,
        firstName: String? = null,
        gender: UserGender? = null,
        lastName: String? = null,
        phoneNumber: String? = null
    ): Single<ResponseResult<UserProfileModel>>

    fun updateUserNoResponse(
        aboutMe: String? = null,
        carLicensePlate: String? = null,
        carModel: String? = null,
        deviceToken: String? = null,
        deviceType: UserDeviceType? = null,
        firstName: String? = null,
        gender: UserGender? = null,
        lastName: String? = null,
        phoneNumber: String? = null
    ): Single<ResponseResult<Unit>>

    fun uploadAvatarImage(imageFile: File): Single<ResponseResult<UserModel>>
}
