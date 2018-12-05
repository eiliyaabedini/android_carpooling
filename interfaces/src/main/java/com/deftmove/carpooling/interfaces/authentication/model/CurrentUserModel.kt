package com.deftmove.carpooling.interfaces.authentication.model

data class CurrentUserModel(
    val apiToken: String,
    val user: UserModel? = null,
    val customer: CustomerModel? = null,
    val isFastLogin: Boolean = false
)
