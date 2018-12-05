package com.deftmove.carpooling.commonui.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.children
import com.deftmove.carpooling.commonui.plugin.getActionIcons
import com.deftmove.carpooling.interfaces.common.data.BadgeIconModel
import com.deftmove.carpooling.interfaces.common.data.IconsType
import com.deftmove.carpooling.interfaces.plugin.Registerer
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import org.koin.core.KoinComponent
import org.koin.core.inject

class VerticalIcons @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), KoinComponent {

    private val registerer: Registerer by inject()
    private val reactiveTransformer: ReactiveTransformer by inject()

    private val disposables = CompositeDisposable()

    init {
        orientation = HORIZONTAL
    }

    fun bind(type: IconsType) {
        when (type) {
            is IconsType.HomeScreenActionBar -> {
                registerActionIcons<BadgeIconModel.BadgeIconHome>(type)

                observeTheType<BadgeIconModel.BadgeIconHome>()
            }

            is IconsType.ProfilePublicScreenActionBar -> {
                registerActionIcons<BadgeIconModel.BadgeIconPublicProfile>(type)

                observeTheType<BadgeIconModel.BadgeIconPublicProfile>()
            }
        }
    }

    private inline fun <reified T : BadgeIconModel> observeTheType() {
        disposables += registerer.observeActionIcons()
              .ofType<T>()
              .observeOn(reactiveTransformer.mainThreadScheduler())
              .subscribe { model ->
                  updateBadge(model)
              }
    }

    private inline fun <reified T : BadgeIconModel> registerActionIcons(type: IconsType) {
        registerer.getActionIcons<T>()
              .sortedByDescending { it.order }
              .forEach { model ->
                  add(model, type)
              }
    }

    //TODO clean up this part.... Please :D
    private fun <T : BadgeIconModel> add(badgeIconModel: T, type: IconsType) {
        addView(
              BadgedIcon(context).apply {
                  bind(badgeIconModel)

                  when (badgeIconModel) {
                      is BadgeIconModel.BadgeIconHome -> {
                          setOnClickListener {
                              badgeIconModel.clickAction(type as IconsType.HomeScreenActionBar)
                          }

                          setOnLongClickListener {
                              badgeIconModel.longClick()
                          }
                      }

                      is BadgeIconModel.BadgeIconPublicProfile -> {
                          setOnClickListener {
                              badgeIconModel.clickAction(type as IconsType.ProfilePublicScreenActionBar)
                          }

                          setOnLongClickListener {
                              badgeIconModel.longClick()
                          }
                      }
                  }

              }
        )
    }

    private fun updateBadge(badgeIconModel: BadgeIconModel) {
        children.map { it as BadgedIcon }
              .first { it.getBadgeIconModel().tagName == badgeIconModel.tagName }
              .bind(badgeIconModel)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        disposables.clear()
    }
}
