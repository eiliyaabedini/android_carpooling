package com.deftmove.carpooling.interfaces.user

import com.deftmove.carpooling.interfaces.authentication.model.CurrentUserModel
import com.deftmove.carpooling.interfaces.authentication.model.CustomerModel
import com.deftmove.carpooling.interfaces.authentication.model.UserModel
import io.reactivex.Observable

interface CurrentUserManager {

    fun getUserModel(): UserModel?

    fun getCustomerModel(): CustomerModel?

    fun getApiToken(): String?

    fun setApiToke(apiToken: String)

    fun changes(): Observable<CurrentUserModel>

    fun setCurrentUser(currentUserModel: CurrentUserModel)

    fun setUser(userModel: UserModel)

    fun setCustomer(customerModel: CustomerModel)

    fun setIsFastLogin(isFastLogin: Boolean)

    fun clearCurrentUser()

    fun isAuthenticated(): Boolean

    fun isProfileComplete(): Boolean

    fun isFastLogin(): Boolean
}
