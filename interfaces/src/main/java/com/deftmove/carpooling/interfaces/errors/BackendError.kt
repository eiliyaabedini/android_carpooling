package com.deftmove.carpooling.interfaces.errors

data class BackendError(
    val message: String?,
    val type: String?,
    val errorCode: String
)
