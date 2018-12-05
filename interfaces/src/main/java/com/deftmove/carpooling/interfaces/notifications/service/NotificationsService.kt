package com.deftmove.carpooling.interfaces.notifications.service

import com.deftmove.carpooling.interfaces.notifications.model.NotificationModel
import com.deftmove.heart.interfaces.ResponseResult
import io.reactivex.Single

interface NotificationsService {

    fun getNotificationsFeed(limit: Int, offset: Int): Single<ResponseResult<List<NotificationModel>>>

    fun getNotificationsUnseenCount(): Single<ResponseResult<Int>>

    fun markAllNotificationsAsSeen(): Single<ResponseResult<Boolean>>
}
