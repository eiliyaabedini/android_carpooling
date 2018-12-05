package com.deftmove.notifications.di

import com.deftmove.carpooling.interfaces.notifications.repository.NotificationsApiPagingRepository
import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.heart.interfaces.koin.Qualifiers
import com.deftmove.heart.interfaces.koin.getAuthenticatedUserScope
import com.deftmove.notifications.NotificationsActivity
import com.deftmove.notifications.NotificationsPresenter
import com.deftmove.notifications.repository.NotificationsApiPagingRepositoryImp
import org.koin.core.qualifier.named
import org.koin.dsl.module

val notificationsModule = module {
    scope(named<NotificationsActivity>()) {
        scoped<Presenter<NotificationsPresenter.View>> {
            NotificationsPresenter(getAuthenticatedUserScope().get(), get(), get(), get(), get())
        }
    }

    scope(Qualifiers.authenticatedUser) {
        scoped<NotificationsApiPagingRepository> {
            NotificationsApiPagingRepositoryImp(get(), get(), get())
        }
    }
}
