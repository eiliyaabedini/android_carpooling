package com.deftmove.services.ride.details

import com.apollographql.apollo.ApolloClient
import com.deftmove.carpooling.interfaces.ride.details.model.RideForDetails
import com.deftmove.carpooling.interfaces.ride.details.service.RideDetailsService
import com.deftmove.carpooling.interfaces.service.rx.AsyncApollo
import com.deftmove.carpooling.ride.details.FindRideDetailsQuery
import com.deftmove.carpooling.type.FindRideInput
import com.deftmove.heart.common.event.EventManager
import com.deftmove.heart.errorhandler.GenericErrorHandler
import com.deftmove.heart.interfaces.ResponseResult
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.rx.doOnFailure
import com.deftmove.heart.interfaces.common.rx.mapData
import com.deftmove.services.extension.convert
import io.reactivex.Single

class RideDetailsServiceImp(
    private val defaultApolloClient: ApolloClient,
    private val errorHandler: GenericErrorHandler,
    private val eventManager: EventManager,
    private val reactiveTransformer: ReactiveTransformer
) : RideDetailsService {

    override fun getRideDetails(rideId: String): Single<ResponseResult<RideForDetails>> {
        val query = FindRideDetailsQuery.builder()
              .input(
                    FindRideInput.builder()
                          .id(rideId)
                          .build()
              )
              .build()

        return AsyncApollo.fromSingle(defaultApolloClient.query(query))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData { it.findRide()!!.ride().fragments().rideForDetailsFragment().convert() }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }
}
