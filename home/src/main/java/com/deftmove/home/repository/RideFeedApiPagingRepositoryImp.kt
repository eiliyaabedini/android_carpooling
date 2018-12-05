package com.deftmove.home.repository

import com.deftmove.carpooling.interfaces.feed.RidesFeedService
import com.deftmove.carpooling.interfaces.pushnotification.model.PushNotificationEvents
import com.deftmove.carpooling.interfaces.repository.RideFeedApiPagingRepository
import com.deftmove.carpooling.interfaces.ride.model.RideForFeed
import com.deftmove.heart.common.event.EventManager
import com.deftmove.heart.common.repository.InMemoryApiPagingRepository
import com.deftmove.heart.interfaces.ResponseResult
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import io.reactivex.Single

class RideFeedApiPagingRepositoryImp(
    private val ridesFeedService: RidesFeedService,
    eventManager: EventManager,
    private val reactiveTransformer: ReactiveTransformer
) : InMemoryApiPagingRepository<RideForFeed>(), RideFeedApiPagingRepository {

    init {
        eventManager.observe()
              .filter { dataEvent ->
                  dataEvent is PushNotificationEvents.InvitationCancelled ||
                        dataEvent is PushNotificationEvents.InvitationConfirmed ||
                        dataEvent is PushNotificationEvents.InvitationCreated ||
                        dataEvent is PushNotificationEvents.RideCreated ||
                        dataEvent is PushNotificationEvents.RideUpdated ||
                        dataEvent is PushNotificationEvents.RideCancelled
              }
              .doOnNext { fetch() }
              .subscribe()
    }

    override fun getDataByPageNumber(pageNumber: Int): Single<ResponseResult<List<RideForFeed>>> {
        return ridesFeedService.getRideFeed(
              limit = PAGE_SIZE,
              offset = pageNumber * PAGE_SIZE
        ).subscribeOn(reactiveTransformer.ioScheduler())
    }

    override fun fetch() {
        super.fetch(0)
    }

    override fun removeByRideId(cancelledRideId: String) {
        val removedRideIndex = getAllItems().indexOfFirst { it.id == cancelledRideId }
        if (removedRideIndex != -1) {
            removeItemFromPageByIndex(0, removedRideIndex)
        }
    }

    companion object {
        private const val PAGE_SIZE: Int = 15
    }
}
