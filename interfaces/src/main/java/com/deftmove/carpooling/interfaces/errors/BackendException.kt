package com.deftmove.carpooling.interfaces.errors

data class BackendException(
    val errors: List<BackendError>
) : Throwable()
