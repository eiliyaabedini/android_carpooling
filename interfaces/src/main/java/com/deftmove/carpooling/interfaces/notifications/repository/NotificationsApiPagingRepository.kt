package com.deftmove.carpooling.interfaces.notifications.repository

import com.deftmove.carpooling.interfaces.notifications.model.NotificationModel
import io.reactivex.Observable

interface NotificationsApiPagingRepository {

    fun fetchNotifications()

    fun observe(): Observable<Unit>

    fun getAllItems(): List<NotificationModel>
}
