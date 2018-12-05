package com.deftmove.app.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.deftmove.carpooling.commonui.factory.NotificationFactory
import com.deftmove.carpooling.interfaces.OpenRideDetails
import com.deftmove.carpooling.interfaces.OpenRideFeedOrRegistrationScreen
import com.deftmove.carpooling.interfaces.pushnotification.model.PushNotificationEvents
import com.deftmove.heart.common.event.EventManager
import com.deftmove.heart.interfaces.common.ForegroundActivityService
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import com.deftmove.heart.interfaces.pushnotification.InternalPushNotificationManager
import com.deftmove.home.R
import timber.log.Timber

class FirebaseMessagingServiceDispatcher(
    val eventManager: EventManager,
    val heartNavigator: HeartNavigator,
    val foregroundActivityService: ForegroundActivityService,
    val internalPushNotificationManager: InternalPushNotificationManager,
    val context: Context
) {

    fun onMessageReceived(
        notificationKey: String,
        title: String?,
        message: String?,
        extraValues: Map<String, String?>
    ) {

        val rideId = extraValues["ride_id"]
        if (notificationKey.isNotEmpty() && !rideId.isNullOrEmpty()) {

            eventManager.notify(
                  PushNotificationEvents.RideNotificationReceived(
                        rideId = rideId
                  )
            )

            showNotificationForRide(notificationKey, title, message, rideId, notificationKey)
        }
    }

    private fun showNotificationForRide(
        notificationKey: String,
        title: String?,
        body: String?,
        rideId: String,
        collapseKey: String?
    ) {
        if (foregroundActivityService.isApplicationVisible()) {
            showNotificationForRideInside(notificationKey, title, body, rideId)
        } else {
            showNotificationForRideOutside(title, body, rideId, collapseKey)
        }
    }

    private fun showNotificationForRideInside(
        notificationKey: String,
        title: String?,
        body: String?,
        rideId: String
    ) {
        val type = NotificationFactory.getNotificationTypeWithNotificationKey(
              notificationKey = notificationKey,
              title = title,
              body = body,
              rideId = rideId
        )

        type?.let { notificationType ->
            internalPushNotificationManager.showInternalPushNotification(
                  notificationType
            ) { activity, alerter ->
                if (activity.getCurrentScreenBucketModel().scope.id.contains("RideDetailsActivity")) {
                    if (activity.getActivityTag() == notificationType.extraValues["rideId"]) {
                        alerter.hide()
                    } else {
                        alerter.hide()
                        heartNavigator.getLauncher(
                              OpenRideDetails(notificationType.extraValues.getValue("rideId")!!)
                        )?.startActivity()
                        activity.finishActivity()
                    }
                } else {
                    alerter.hide()
                    heartNavigator.getLauncher(
                          OpenRideDetails(notificationType.extraValues.getValue("rideId")!!)
                    )?.startActivity()
                }
            }
        }
    }

    private fun showNotificationForRideOutside(
        title: String?,
        body: String?,
        rideId: String,
        collapseKey: String?
    ) {

        Timber.d("Message Notification title:$title, body:$body")

        val pendingIntent: PendingIntent? = getRideDetailsPendingIntent(rideId)

        val channelId = context.getString(R.string.default_notification_channel_id)
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
              .setSmallIcon(R.drawable.ic_company_logo)
              .setContentTitle(title)
              .setContentText(body)
              .setAutoCancel(true)

        notificationBuilder?.setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                  channelId,
                  "Default notifications",
                  NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        if (collapseKey.isNullOrEmpty()) {
            notificationManager.notify((rideId + body + title).hashCode(), notificationBuilder.build())
        } else {
            notificationManager.notify(collapseKey.hashCode(), notificationBuilder.build())
        }
    }

    private fun getRideDetailsPendingIntent(rideId: String): PendingIntent? {
        return TaskStackBuilder.create(context)
              .addNextIntent(
                    heartNavigator.getLauncher(OpenRideFeedOrRegistrationScreen)!!
                          .getIntent()
              )
              .addNextIntent(
                    heartNavigator.getLauncher(OpenRideDetails(rideId = rideId))!!
                          .getIntent()
              )
              .getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT)
    }
}
