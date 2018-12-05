package com.deftmove.carpooling.interfaces.notifications.model

import com.github.vivchar.rendererrecyclerviewadapter.ViewModel
import java.util.Date

data class NotificationModel(
    val id: String,
    val message: String,
    val title: String?,
    val insertedAt: Date,
    val seen: Boolean,
    val data: List<NotificationDataModel>
) : ViewModel {

    override fun equals(other: Any?): Boolean {
        return other is NotificationModel &&
              other.id == this.id &&
              other.message == this.message &&
              other.title == this.title &&
              other.insertedAt == this.insertedAt &&
              other.seen == this.seen &&
              other.data == this.data
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
