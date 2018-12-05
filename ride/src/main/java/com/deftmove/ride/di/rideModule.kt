package com.deftmove.ride.di

import com.deftmove.carpooling.interfaces.ride.repository.WaypointsUpdateRepository
import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.ride.create.CreateRideActivity
import com.deftmove.ride.create.CreateRidePresenter
import com.deftmove.ride.repository.WaypointsUpdateRepositoryImp
import com.deftmove.ride.review.CreateRideReviewActivity
import com.deftmove.ride.review.CreateRideReviewPresenter
import com.deftmove.ride.waypoints.WayPointsActivity
import com.deftmove.ride.waypoints.WayPointsActivityPresenter
import org.koin.core.qualifier.named
import org.koin.dsl.module

val rideModule = module {

    scope(named<CreateRideActivity>()) {
        scoped<Presenter<CreateRidePresenter.View>> {
            CreateRidePresenter(get(), get(), get(), get(), get())
        }
    }

    scope(named<WayPointsActivity>()) {
        scoped<Presenter<WayPointsActivityPresenter.View>> {
            WayPointsActivityPresenter(get(), get(), get(), get(), get())
        }
    }

    scope(named<CreateRideReviewActivity>()) {
        scoped<Presenter<CreateRideReviewPresenter.View>> {
            CreateRideReviewPresenter(get(), get(), get(), get())
        }
    }

    single<WaypointsUpdateRepository> { WaypointsUpdateRepositoryImp() }
}
