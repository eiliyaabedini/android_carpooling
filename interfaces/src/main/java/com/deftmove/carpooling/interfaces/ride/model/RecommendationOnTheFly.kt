package com.deftmove.carpooling.interfaces.ride.model

import com.deftmove.heart.interfaces.map.Location

data class RecommendationOnTheFly(
    val location: Location,
    val userId: String,
    val userAvatar: String?,
    val firstName: String
)
