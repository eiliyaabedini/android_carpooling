package com.deftmove.carpooling.commonui.factory

import com.deftmove.heart.interfaces.pushnotification.model.AlerterColorType
import com.deftmove.heart.interfaces.pushnotification.model.NotificationType

object NotificationFactory {

    fun getNotificationTypeWithNotificationKey(
        notificationKey: String,
        title: String?,
        body: String?,
        rideId: String
    ): NotificationType? {
        return when (notificationKey) {
            "invitation_created" -> NotificationType(
                  notificationKey = "invitation_created",
                  notificationTitle = title,
                  notificationBody = body ?: "",
                  alerterColorType = AlerterColorType.BLACK,
                  notificationIconRes = null,
                  extraValues = mapOf("rideId" to rideId)
            )

            "invitation_confirmed" -> NotificationType(
                  notificationKey = "invitation_confirmed",
                  notificationTitle = title,
                  notificationBody = body ?: "",
                  alerterColorType = AlerterColorType.SUCCESS,
                  notificationIconRes = null,
                  extraValues = mapOf("rideId" to rideId)
            )

            "invitation_cancelled" -> NotificationType(
                  notificationKey = "invitation_cancelled",
                  notificationTitle = title,
                  notificationBody = body ?: "",
                  alerterColorType = AlerterColorType.ERROR,
                  notificationIconRes = null,
                  extraValues = mapOf("rideId" to rideId)
            )

            "ride_cancelled" -> NotificationType(
                  notificationKey = "ride_cancelled",
                  notificationTitle = title,
                  notificationBody = body ?: "",
                  alerterColorType = AlerterColorType.ERROR,
                  notificationIconRes = null,
                  extraValues = mapOf("rideId" to rideId)
            )

            "start_reminder" -> NotificationType(
                  notificationKey = "start_reminder",
                  notificationTitle = title,
                  notificationBody = body ?: "",
                  alerterColorType = AlerterColorType.BLACK,
                  notificationIconRes = null,
                  extraValues = mapOf("rideId" to rideId)
            )

            "match_found" -> NotificationType(
                  notificationKey = "match_found",
                  notificationTitle = title,
                  notificationBody = body ?: "",
                  alerterColorType = AlerterColorType.BLACK,
                  notificationIconRes = null,
                  extraValues = mapOf("rideId" to rideId)
            )

            "conversation_message_received" -> NotificationType(
                  notificationKey = "conversation_message_received",
                  notificationTitle = title,
                  notificationBody = body ?: "",
                  alerterColorType = AlerterColorType.BLACK,
                  notificationIconRes = null,
                  extraValues = mapOf("rideId" to rideId)
            )

            else -> null
        }
    }
}
