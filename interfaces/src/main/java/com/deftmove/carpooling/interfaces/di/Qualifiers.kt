package com.deftmove.carpooling.interfaces.di

import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named

object Qualifiers {
    val defaultApollo: Qualifier = named("defaultApollo")

    val apolloCustomAdapterId: Qualifier = named("apolloCustomAdapterId")
    val apolloCustomAdapterDateTime: Qualifier = named("apolloCustomAdapterDateTime")
}
