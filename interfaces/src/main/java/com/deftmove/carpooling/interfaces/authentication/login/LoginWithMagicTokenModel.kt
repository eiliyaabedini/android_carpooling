package com.deftmove.carpooling.interfaces.authentication.login

import java.io.Serializable

data class LoginWithMagicTokenModel(
    val magicToken: String
) : Serializable
