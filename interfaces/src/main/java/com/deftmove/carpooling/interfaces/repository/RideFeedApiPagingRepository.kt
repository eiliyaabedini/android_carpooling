package com.deftmove.carpooling.interfaces.repository

import com.deftmove.carpooling.interfaces.ride.model.RideForFeed
import io.reactivex.Observable

interface RideFeedApiPagingRepository {

    fun fetch()

    fun observe(): Observable<Unit>

    fun removeByRideId(cancelledRideId: String)
    fun getAllItems(): List<RideForFeed>
}
