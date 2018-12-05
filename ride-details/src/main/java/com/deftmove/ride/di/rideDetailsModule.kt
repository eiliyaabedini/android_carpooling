package com.deftmove.ride.di

import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.ride.details.RideDetailsActivity
import com.deftmove.ride.details.RideDetailsPresenter
import org.koin.core.qualifier.named
import org.koin.dsl.module

val rideDetailsModule = module {
    scope(named<RideDetailsActivity>()) {
        scoped<Presenter<RideDetailsPresenter.View>> {
            RideDetailsPresenter(get(), get(), get(), get(), get(), get())
        }
    }
}
