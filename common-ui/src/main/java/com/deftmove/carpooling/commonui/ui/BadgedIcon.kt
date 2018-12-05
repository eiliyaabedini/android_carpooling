package com.deftmove.carpooling.commonui.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isGone
import com.deftmove.carpooling.commonui.R
import com.deftmove.carpooling.interfaces.common.data.BadgeIconModel
import com.deftmove.heart.common.ui.extension.bindView

class BadgedIcon @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val iconImageView: AppCompatImageView by bindView(R.id.badged_icon_image_view)
    private val badgeView: AppCompatTextView by bindView(R.id.badged_icon_badge)
    private lateinit var badgeIconModel: BadgeIconModel

    init {
        if (!isInEditMode) {
            LayoutInflater.from(context)
                  .inflate(R.layout.badged_icon_view_layout, this, true)
        }
    }

    fun bind(badgeIconModel: BadgeIconModel) {
        this.badgeIconModel = badgeIconModel
        iconImageView.setImageResource(badgeIconModel.iconResId)
        updateBadge(badgeIconModel.badgeText)
    }

    private fun updateBadge(badgeText: String?) {
        if (badgeText.isNullOrBlank()) {
            badgeView.isGone = true
        } else {
            badgeView.isGone = false
            badgeView.text = badgeText
        }
    }

    fun getBadgeIconModel(): BadgeIconModel = badgeIconModel
}
