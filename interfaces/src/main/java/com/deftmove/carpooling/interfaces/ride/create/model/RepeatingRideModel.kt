package com.deftmove.carpooling.interfaces.ride.create.model

import java.io.Serializable

data class RepeatingRideModel(
    var monday: Boolean = false,
    var tuesday: Boolean = false,
    var wednesday: Boolean = false,
    var thursday: Boolean = false,
    var friday: Boolean = false,
    var saturday: Boolean = false,
    var sunday: Boolean = false
) : Serializable
