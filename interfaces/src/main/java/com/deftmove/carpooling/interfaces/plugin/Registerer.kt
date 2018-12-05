package com.deftmove.carpooling.interfaces.plugin

import com.deftmove.carpooling.interfaces.common.data.BadgeIconModel
import io.reactivex.Observable

interface Registerer {

    fun registerActionIcon(badgeIconModel: BadgeIconModel)
    fun notifyUpdate(badgeIconModel: BadgeIconModel)

    fun getAllActionIcons(): List<BadgeIconModel>
    fun observeActionIcons(): Observable<BadgeIconModel>

}
