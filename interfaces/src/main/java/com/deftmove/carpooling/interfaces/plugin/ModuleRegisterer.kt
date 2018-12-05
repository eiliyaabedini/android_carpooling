package com.deftmove.carpooling.interfaces.plugin

import com.deftmove.heart.interfaces.navigator.HeartNavigator
import org.koin.core.KoinComponent
import org.koin.core.module.Module

interface ModuleRegisterer : KoinComponent {

    fun getKoinModule(): Module

    fun registerNavigators(heartNavigator: HeartNavigator)

    fun register(registerer: Registerer, heartNavigator: HeartNavigator)
}

