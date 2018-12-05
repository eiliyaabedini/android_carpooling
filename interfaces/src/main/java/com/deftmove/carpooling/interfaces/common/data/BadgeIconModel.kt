package com.deftmove.carpooling.interfaces.common.data

import android.view.View
import androidx.annotation.DrawableRes

sealed class BadgeIconModel(
    open val order: Int,
    open val tagName: String,
    @DrawableRes open val iconResId: Int,
    open val visibility: Int = View.VISIBLE,
    open val badgeText: String? = null
) {

    data class BadgeIconHome(
        override val order: Int,
        override val tagName: String,
        @DrawableRes override val iconResId: Int,
        override val visibility: Int = View.VISIBLE,
        override val badgeText: String? = null,
        val clickAction: (IconsType.HomeScreenActionBar) -> Unit,
        val longClick: () -> Boolean = { false }
    ) : BadgeIconModel(order, tagName, iconResId, visibility, badgeText)

    data class BadgeIconPublicProfile(
        override val order: Int,
        override val tagName: String,
        @DrawableRes override val iconResId: Int,
        override val visibility: Int = View.VISIBLE,
        override val badgeText: String? = null,
        val clickAction: (IconsType.ProfilePublicScreenActionBar) -> Unit,
        val longClick: () -> Boolean = { false }
    ) : BadgeIconModel(order, tagName, iconResId, visibility, badgeText)
}
