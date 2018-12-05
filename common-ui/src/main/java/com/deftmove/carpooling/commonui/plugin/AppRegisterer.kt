package com.deftmove.carpooling.commonui.plugin

import com.deftmove.carpooling.interfaces.common.data.BadgeIconModel
import com.deftmove.carpooling.interfaces.plugin.Registerer
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class AppRegisterer : Registerer {

    private val registeredActionIcons = mutableListOf<BadgeIconModel>()
    private val registeredActionIconsSubject: PublishSubject<BadgeIconModel> = PublishSubject.create()

    override fun registerActionIcon(badgeIconModel: BadgeIconModel) {
        registeredActionIcons.add(badgeIconModel)
        registeredActionIconsSubject.onNext(badgeIconModel)
    }

    override fun notifyUpdate(badgeIconModel: BadgeIconModel) {
        val index = registeredActionIcons.indexOfFirst { it.tagName == badgeIconModel.tagName }
        if (index >= 0) {
            registeredActionIcons.set(index, badgeIconModel)
        }

        registeredActionIconsSubject.onNext(badgeIconModel)
    }

    override fun getAllActionIcons(): List<BadgeIconModel> = registeredActionIcons
    override fun observeActionIcons(): Observable<BadgeIconModel> = registeredActionIconsSubject
}

inline fun <reified T : BadgeIconModel> Registerer.getActionIcons(): List<T> {
    return getAllActionIcons()
          .filterIsInstance<T>()
}

inline fun <reified T : BadgeIconModel> Registerer.updateActionIcon(tagName: String, updateAction: (T) -> T) {
    val updatedModels = mutableListOf<BadgeIconModel>()

    getActionIcons<T>()
          .map {
              if (it.tagName == tagName) {
                  val newModel = updateAction(it)

                  updatedModels.add(newModel)

                  newModel
              } else it
          }

    updatedModels.forEach {
        notifyUpdate(it)
    }
}
