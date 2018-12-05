package com.deftmove.carpooling.interfaces.ride.details.service

import com.deftmove.carpooling.interfaces.ride.details.model.RideForDetails
import com.deftmove.heart.interfaces.ResponseResult
import io.reactivex.Single

interface RideDetailsService {

    fun getRideDetails(
        rideId: String
    ): Single<ResponseResult<RideForDetails>>
}
