package com.deftmove.services.ride

import com.apollographql.apollo.ApolloClient
import com.deftmove.carpooling.interfaces.pushnotification.model.PushNotificationEvents
import com.deftmove.carpooling.interfaces.ride.create.model.RepeatingRideModel
import com.deftmove.carpooling.interfaces.ride.model.RecommendationOnTheFly
import com.deftmove.carpooling.interfaces.ride.model.RideForFeed
import com.deftmove.carpooling.interfaces.ride.repository.WaypointsUpdateRepository
import com.deftmove.carpooling.interfaces.ride.service.RideService
import com.deftmove.carpooling.interfaces.service.rx.AsyncApollo
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.carpooling.ride.CancelRideMutation
import com.deftmove.carpooling.ride.CreateRideMutation
import com.deftmove.carpooling.ride.FindRoutesQuery
import com.deftmove.carpooling.ride.GetRecommendationsOnTheFlyQuery
import com.deftmove.carpooling.ride.UpdateRideMutation
import com.deftmove.carpooling.type.CancelRideInput
import com.deftmove.carpooling.type.CreateRideInput
import com.deftmove.carpooling.type.FindRoutesInput
import com.deftmove.carpooling.type.RecommendationsOnTheFlyInput
import com.deftmove.carpooling.type.Role
import com.deftmove.carpooling.type.UpdateRideInput
import com.deftmove.heart.common.event.EventManager
import com.deftmove.heart.errorhandler.GenericErrorHandler
import com.deftmove.heart.interfaces.ResponseResult
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.rx.doOnData
import com.deftmove.heart.interfaces.common.rx.doOnFailure
import com.deftmove.heart.interfaces.common.rx.mapData
import com.deftmove.heart.interfaces.map.Location
import com.deftmove.heart.interfaces.map.Route
import com.deftmove.services.extension.convert
import com.deftmove.services.extension.toLocationInput
import com.deftmove.services.extension.toStopInput
import io.reactivex.Single
import java.util.Date

class RideServiceImp(
    private val defaultApolloClient: ApolloClient,
    private val errorHandler: GenericErrorHandler,
    private val currentUserManager: CurrentUserManager,
    private val eventManager: EventManager,
    private val waypointsUpdateRepository: WaypointsUpdateRepository,
    private val reactiveTransformer: ReactiveTransformer
) : RideService {

    override fun findRoutes(
        destination: Location,
        origin: Location,
        stops: List<Location>,
        startTime: Date
    ): Single<ResponseResult<List<Route>>> {
        val query = FindRoutesQuery.builder()
              .input(
                    FindRoutesInput.builder()
                          .destination(destination.toLocationInput())
                          .origin(origin.toLocationInput())
                          .stops(stops.map { it.toLocationInput() })
                          .startTime(startTime)
                          .build()
              )
              .build()

        val uniqueId: Int = query.hashCode()
        return AsyncApollo.fromSingle(defaultApolloClient.query(query))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .doOnSubscribe {
                  waypointsUpdateRepository.addWaypoints(uniqueId, stops)
              }
              .mapData { it.findRoutes()!!.convert() }
              .mapData { routes ->
                  waypointsUpdateRepository.updateRoutes(uniqueId, routes)
              }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }

    override fun createRideAsDriver(
        origin: Location,
        destination: Location,
        startTime: Date,
        route: Route?,
        repeat: RepeatingRideModel
    ): Single<ResponseResult<RideForFeed>> {
        val mutation = CreateRideMutation.builder()
              .input(
                    CreateRideInput.builder()
                          .origin(origin.toLocationInput())
                          .destination(destination.toLocationInput())
                          .role(Role.DRIVER)
                          .time(startTime)
                          .distance(route?.distance)
                          .duration(route?.duration)
                          .polyline(route?.polyline)
                          .repeat(repeat.convert())
                          .farePerKm(currentUserManager.getCustomerModel()?.farePerKm?.amountCents)
                          .stops(route?.stops?.map { it.toStopInput() })
                          .build()
              )
              .build()

        return AsyncApollo.fromSingle(defaultApolloClient.mutate(mutation))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData { it.createRide()!!.ride().fragments().rideForFeedFragment().convert() }
              .doOnData { eventManager.notify(PushNotificationEvents.RideCreated) }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }

    override fun updateRideAsDriver(
        id: String,
        updateRepeatingRides: Boolean?,
        origin: Location,
        destination: Location,
        startTime: Date,
        route: Route?,
        repeat: RepeatingRideModel
    ): Single<ResponseResult<RideForFeed>> {
        val mutation = UpdateRideMutation.builder()
              .input(
                    UpdateRideInput.builder()
                          .id(id)
                          .updateRepeatingRides(updateRepeatingRides)
                          .origin(origin.toLocationInput())
                          .destination(destination.toLocationInput())
                          .time(startTime)
                          .distance(route?.distance)
                          .duration(route?.duration)
                          .polyline(route?.polyline)
                          .repeat(repeat.convert())
                          .farePerKm(currentUserManager.getCustomerModel()?.farePerKm?.amountCents)
                          .stops(route?.stops?.map { it.toStopInput() })
                          .build()
              )
              .build()

        return AsyncApollo.fromSingle(defaultApolloClient.mutate(mutation))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData { it.updateRide()!!.ride().fragments().rideForFeedFragment().convert() }
              .doOnData { eventManager.notify(PushNotificationEvents.RideUpdated) }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }

    override fun createRideAsPassenger(
        origin: Location,
        destination: Location,
        startTime: Date,
        repeat: RepeatingRideModel
    ): Single<ResponseResult<RideForFeed>> {
        val mutation = CreateRideMutation.builder()
              .input(
                    CreateRideInput.builder()
                          .origin(origin.toLocationInput())
                          .destination(destination.toLocationInput())
                          .role(Role.PASSENGER)
                          .repeat(repeat.convert())
                          .time(startTime)
                          .build()
              )
              .build()

        return AsyncApollo.fromSingle(defaultApolloClient.mutate(mutation))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData { it.createRide()!!.ride().fragments().rideForFeedFragment().convert() }
              .doOnData { eventManager.notify(PushNotificationEvents.RideCreated) }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }

    override fun updateRideAsPassenger(
        id: String,
        updateRepeatingRides: Boolean?,
        origin: Location,
        destination: Location,
        startTime: Date,
        repeat: RepeatingRideModel
    ): Single<ResponseResult<RideForFeed>> {
        val mutation = UpdateRideMutation.builder()
              .input(
                    UpdateRideInput.builder()
                          .id(id)
                          .updateRepeatingRides(updateRepeatingRides)
                          .origin(origin.toLocationInput())
                          .destination(destination.toLocationInput())
                          .time(startTime)
                          .repeat(repeat.convert())
                          .build()
              )
              .build()

        return AsyncApollo.fromSingle(defaultApolloClient.mutate(mutation))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData { it.updateRide()!!.ride().fragments().rideForFeedFragment().convert() }
              .doOnData { eventManager.notify(PushNotificationEvents.RideCreated) }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }

    override fun cancelRide(rideId: String, cancelRepeatingRides: Boolean): Single<ResponseResult<String>> {
        val mutation = CancelRideMutation.builder()
              .input(
                    CancelRideInput.builder()
                          .id(rideId)
                          .cancelRepeatingRides(cancelRepeatingRides)
                          .build()
              )
              .build()

        return AsyncApollo.fromSingle(defaultApolloClient.mutate(mutation))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData { it.cancelRide()!!.ride().id() }
              .doOnData { eventManager.notify(PushNotificationEvents.RideCancelled(it)) }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }

    override fun getRecommendationsOnTheFlyForDriver(
        destination: Location,
        origin: Location,
        polyline: String?,
        startTime: Date
    ): Single<ResponseResult<List<RecommendationOnTheFly>>> {
        val query = GetRecommendationsOnTheFlyQuery.builder()
              .input(
                    RecommendationsOnTheFlyInput.builder()
                          .origin(origin.toLocationInput())
                          .destination(destination.toLocationInput())
                          .polyline(polyline)
                          .time(startTime)
                          .role(Role.DRIVER)
                          .limit(limit)
                          .offset(offset)
                          .routeOffset(routeOffset)
                          .farePerKm(currentUserManager.getCustomerModel()?.farePerKm?.amountCents)
                          .build()
              )
              .build()

        return AsyncApollo.fromSingle(defaultApolloClient.query(query))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData {
                  it.recommendationsOnTheFly()!!.recommendations()!!.map { recommendation ->
                      RecommendationOnTheFly(
                            location = recommendation.origin().fragments().locationFragment().convert(),
                            userId = recommendation.user().id(),
                            userAvatar = recommendation.user().avatarUrl(),
                            firstName = recommendation.user().firstname() ?: ""
                      )
                  }
              }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }

    companion object {
        private const val limit: Int = 20
        private const val offset: Int = 0
        private const val routeOffset: Double = 0.025
    }
}
