package com.deftmove.notifications.renderer

import android.view.View
import android.widget.TextView
import androidx.core.view.isGone
import com.deftmove.carpooling.interfaces.notifications.model.NotificationModel
import com.deftmove.heart.common.extension.toRelativeDateString
import com.deftmove.heart.common.extension.toTimeString
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.notifications.NotificationsPresenter
import com.deftmove.notifications.R
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewBinder
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewFinder
import io.reactivex.subjects.Subject

class NotificationsFeedRenderer(
    private val actions: Subject<PresenterAction>
) : ViewBinder.Binder<NotificationModel> {

    override fun bindView(notificationModel: NotificationModel, finder: ViewFinder, payloads: MutableList<Any>) {
        finder.find<View>(R.id.notifications_feed_renderer_root).setOnClickListener {
            actions.onNext(NotificationsPresenter.Action.NotificationItemClicked(notificationModel.data))
        }

        finder.find<View>(R.id.notifications_feed_renderer_seen).isGone = notificationModel.seen
        finder.find<TextView>(R.id.notifications_feed_renderer_title).text = notificationModel.title
        finder.find<TextView>(R.id.notifications_feed_renderer_message).text = notificationModel.message
        finder.find<TextView>(R.id.notifications_feed_renderer_date).apply {
            text = context.getString(
                  R.string.notifications_screen_item_time,
                  notificationModel.insertedAt.toRelativeDateString(context),
                  notificationModel.insertedAt.toTimeString(context)
            )
        }
    }
}
