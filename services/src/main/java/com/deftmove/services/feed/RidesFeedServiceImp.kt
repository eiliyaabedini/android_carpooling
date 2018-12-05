package com.deftmove.services.feed

import com.apollographql.apollo.ApolloClient
import com.deftmove.carpooling.feed.RidesFeedQuery
import com.deftmove.carpooling.interfaces.feed.RidesFeedService
import com.deftmove.carpooling.interfaces.ride.model.RideForFeed
import com.deftmove.carpooling.interfaces.service.rx.AsyncApollo
import com.deftmove.carpooling.type.RidesFeedInput
import com.deftmove.heart.errorhandler.GenericErrorHandler
import com.deftmove.heart.interfaces.ResponseResult
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.rx.doOnFailure
import com.deftmove.heart.interfaces.common.rx.mapData
import com.deftmove.services.extension.convert
import io.reactivex.Single

class RidesFeedServiceImp(
    private val defaultApolloClient: ApolloClient,
    private val errorHandler: GenericErrorHandler,
    private val reactiveTransformer: ReactiveTransformer
) : RidesFeedService {

    override fun getRideFeed(limit: Int, offset: Int): Single<ResponseResult<List<RideForFeed>>> {
        val query = RidesFeedQuery.builder()
              .input(
                    RidesFeedInput.builder()
                          .limit(limit)
                          .offset(offset)
                          .build()
              )
              .build()

        return AsyncApollo.fromSingle(defaultApolloClient.query(query))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData {
                  it.ridesFeed()!!.rides()!!.map { ride ->
                      ride.fragments().rideForFeedFragment().convert()
                  }
              }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }
}
