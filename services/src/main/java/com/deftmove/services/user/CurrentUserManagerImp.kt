package com.deftmove.services.user

import com.deftmove.carpooling.interfaces.authentication.model.CurrentUserModel
import com.deftmove.carpooling.interfaces.authentication.model.CustomerModel
import com.deftmove.carpooling.interfaces.authentication.model.UserModel
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.heart.interfaces.common.LocalStorageManager
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class CurrentUserManagerImp(
    private val localStorageManager: LocalStorageManager
) : CurrentUserManager {

    private val updateSubject: BehaviorSubject<CurrentUserModel> = BehaviorSubject.create()

    init {
        getCurrentUser()?.let { updateSubject.onNext(it) }
    }

    private fun getCurrentUser(): CurrentUserModel? {
        return localStorageManager.getByKey(CURRENT_USER_MODEL, CurrentUserModel::class.java)
    }

    override fun getUserModel(): UserModel? = getCurrentUser()?.user

    override fun getCustomerModel(): CustomerModel? = getCurrentUser()?.customer

    override fun getApiToken(): String? = getCurrentUser()?.apiToken

    override fun changes(): Observable<CurrentUserModel> = updateSubject
          .distinctUntilChanged()

    override fun setCurrentUser(currentUserModel: CurrentUserModel) {
        localStorageManager.saveByKey(CURRENT_USER_MODEL, currentUserModel)

        updateSubject.onNext(currentUserModel)
    }

    override fun setUser(userModel: UserModel) {
        setCurrentUser(getSafeCurrentUser().copy(user = userModel))
    }

    override fun setCustomer(customerModel: CustomerModel) {
        setCurrentUser(getSafeCurrentUser().copy(customer = customerModel))
    }

    override fun setIsFastLogin(isFastLogin: Boolean) {
        setCurrentUser(getSafeCurrentUser().copy(isFastLogin = isFastLogin))
    }

    override fun setApiToke(apiToken: String) {
        setCurrentUser(getSafeCurrentUser().copy(apiToken = apiToken))
    }

    private fun getSafeCurrentUser(): CurrentUserModel {
        if (getCurrentUser() == null) {
            setCurrentUser(CurrentUserModel(""))
        }

        return getCurrentUser()!!
    }

    override fun clearCurrentUser() {
        localStorageManager.removeByKey(CURRENT_USER_MODEL)
    }

    override fun isAuthenticated(): Boolean {
        return getCurrentUser()?.apiToken?.isNotBlank() ?: false
    }

    override fun isProfileComplete(): Boolean {
        val currentUser = getCurrentUser()
        return currentUser?.user != null &&
              currentUser.apiToken.isNotBlank() &&
              !currentUser.user!!.firstName.isNullOrBlank() &&
              !currentUser.user!!.lastName.isNullOrBlank() &&
              currentUser.user!!.gender != null
    }

    override fun isFastLogin(): Boolean {
        return getCurrentUser()?.isFastLogin ?: false
    }

    companion object {
        const val CURRENT_USER_MODEL = "com.deftmove.common.CurrentUserModel"
    }
}
