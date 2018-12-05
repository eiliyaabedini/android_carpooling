package com.deftmove.carpooling.interfaces.authentication.model

import java.io.Serializable
import java.util.Date

data class UserModel(
    val id: String,
    val firstName: String?,
    val lastName: String?,
    val email: String,
    val gender: UserGender?,
    val avatarUrl: String?,
    val aboutMe: String?,
    val phoneNumber: String?,
    val carLicensePlate: String?,
    val carModel: String?,
    val memberSince: Date,
    val numberOfRidesAsDriver: Int?,
    val numberOfRidesAsPassenger: Int?
) : Serializable
