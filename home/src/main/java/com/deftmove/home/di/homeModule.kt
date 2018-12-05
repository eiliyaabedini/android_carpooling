package com.deftmove.home.di

import com.deftmove.carpooling.interfaces.repository.RideFeedApiPagingRepository
import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.heart.interfaces.koin.Qualifiers
import com.deftmove.heart.interfaces.koin.getAuthenticatedUserScope
import com.deftmove.home.HomeActivity
import com.deftmove.home.HomePresenter
import com.deftmove.home.delegate.ShowLicenceDelegate
import com.deftmove.home.delegate.ShowLicenceDelegateImp
import com.deftmove.home.repository.RideFeedApiPagingRepositoryImp
import org.koin.core.qualifier.named
import org.koin.dsl.module

val homeModule = module {
    scope(named<HomeActivity>()) {
        scoped<Presenter<HomePresenter.View>> {
            HomePresenter(get(), get(), getAuthenticatedUserScope().get(), get(), get(), get(), get(), get())
        }

        scoped<ShowLicenceDelegate> { ShowLicenceDelegateImp(get(), get()) }
    }

    scope(Qualifiers.authenticatedUser) {
        scoped<RideFeedApiPagingRepository> {
            RideFeedApiPagingRepositoryImp(get(), get(), get())
        }
    }
}
