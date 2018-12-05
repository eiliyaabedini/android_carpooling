package com.deftmove.carpooling.interfaces.feed

import com.deftmove.carpooling.interfaces.ride.model.RideForFeed
import com.deftmove.heart.interfaces.ResponseResult
import io.reactivex.Single

interface RidesFeedService {

    fun getRideFeed(limit: Int, offset: Int): Single<ResponseResult<List<RideForFeed>>>
}
