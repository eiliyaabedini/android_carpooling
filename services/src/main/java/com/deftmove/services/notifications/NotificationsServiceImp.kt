package com.deftmove.services.notifications

import com.apollographql.apollo.ApolloClient
import com.deftmove.carpooling.interfaces.notifications.model.NotificationModel
import com.deftmove.carpooling.interfaces.notifications.service.NotificationsService
import com.deftmove.carpooling.interfaces.service.rx.AsyncApollo
import com.deftmove.carpooling.notifications.HaveSeenNotificationsMutation
import com.deftmove.carpooling.notifications.NotificationsFeedQuery
import com.deftmove.carpooling.notifications.NotificationsUnseenCountQuery
import com.deftmove.carpooling.type.NotificationsFeedInput
import com.deftmove.heart.common.event.DataEvent
import com.deftmove.heart.common.event.EventManager
import com.deftmove.heart.errorhandler.GenericErrorHandler
import com.deftmove.heart.interfaces.ResponseResult
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.rx.doOnData
import com.deftmove.heart.interfaces.common.rx.doOnFailure
import com.deftmove.heart.interfaces.common.rx.mapData
import com.deftmove.services.extension.convert
import io.reactivex.Single

class NotificationsServiceImp(
    private val defaultApolloClient: ApolloClient,
    private val eventManager: EventManager,
    private val errorHandler: GenericErrorHandler,
    private val reactiveTransformer: ReactiveTransformer
) : NotificationsService {

    override fun getNotificationsFeed(limit: Int, offset: Int): Single<ResponseResult<List<NotificationModel>>> {
        val query = NotificationsFeedQuery.builder()
              .input(
                    NotificationsFeedInput.builder()
                          .limit(limit)
                          .offset(offset)
                          .build()
              )
              .build()

        return AsyncApollo.fromSingle(defaultApolloClient.query(query))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData { data ->
                  data.notificationsFeed()!!.notifications()!!.map { notification ->
                      notification.fragments().notificationFragment().convert()
                  }
              }
              .doOnData {
                  val unseenNotificationsCount = it.filter { !it.seen }.count()
                  eventManager.notify(
                        DataEvent.UnreadNotificationsCountChanged(unseenNotificationsCount)
                  )
              }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }

    override fun getNotificationsUnseenCount(): Single<ResponseResult<Int>> {
        val query = NotificationsUnseenCountQuery.builder()
              .build()

        return AsyncApollo.fromSingle(defaultApolloClient.query(query))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData { it.notificationsUnseenCount()!! }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }

    override fun markAllNotificationsAsSeen(): Single<ResponseResult<Boolean>> {
        val mutation = HaveSeenNotificationsMutation.builder()
              .build()

        return AsyncApollo.fromSingle(defaultApolloClient.mutate(mutation))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData { it.haveSeenNotifications()!!.haveSeen()!! }
              .doOnData { eventManager.notify(DataEvent.UnreadNotificationsCountChanged(0)) }
              .doOnData { eventManager.notify(DataEvent.NotificationsSeen) }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }
}
