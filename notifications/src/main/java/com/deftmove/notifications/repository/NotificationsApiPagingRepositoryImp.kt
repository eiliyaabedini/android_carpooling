package com.deftmove.notifications.repository

import com.deftmove.carpooling.interfaces.notifications.model.NotificationModel
import com.deftmove.carpooling.interfaces.notifications.repository.NotificationsApiPagingRepository
import com.deftmove.carpooling.interfaces.notifications.service.NotificationsService
import com.deftmove.carpooling.interfaces.pushnotification.model.PushNotificationEvents
import com.deftmove.heart.common.event.DataEvent
import com.deftmove.heart.common.event.EventManager
import com.deftmove.heart.common.repository.InMemoryApiPagingRepository
import com.deftmove.heart.interfaces.ResponseResult
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import io.reactivex.Single

class NotificationsApiPagingRepositoryImp(
    private val notificationsService: NotificationsService,
    private val eventManager: EventManager,
    private val reactiveTransformer: ReactiveTransformer
) : InMemoryApiPagingRepository<NotificationModel>(),
    NotificationsApiPagingRepository {

    init {
        eventManager.observe()
              .filter { dataEvent ->
                  dataEvent is DataEvent.NotificationReceived ||
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

    override fun getDataByPageNumber(pageNumber: Int): Single<ResponseResult<List<NotificationModel>>> {
        return notificationsService.getNotificationsFeed(
              limit = NUMBER_OF_ITEMS_IN_PAGE,
              offset = pageNumber * NUMBER_OF_ITEMS_IN_PAGE
        )
              .subscribeOn(reactiveTransformer.ioScheduler())
    }

    override fun fetchNotifications() {
        fetch(0)
    }

    companion object {
        private const val NUMBER_OF_ITEMS_IN_PAGE: Int = 50
    }
}
