package com.deftmove.carpooling.interfaces.authentication.services

import com.deftmove.carpooling.interfaces.authentication.model.CurrentUserModel
import com.deftmove.carpooling.interfaces.authentication.model.CustomerModel
import com.deftmove.heart.interfaces.ResponseResult
import io.reactivex.Observable
import io.reactivex.Single

interface AuthenticationService {

    fun createMagicToken(email: String): Observable<ResponseResult<String>>

    fun putMagicTokenLink(magicLink: String, magicToken: String): Observable<ResponseResult<Boolean>>

    fun loginWithMagicToken(magicToken: String): Single<ResponseResult<CurrentUserModel>>

    fun getCurrentCustomer(): Single<ResponseResult<CustomerModel>>

    fun logout(): Single<ResponseResult<Unit>>
}
