package com.deftmove.authentication.di

import com.deftmove.authentication.firebase.FirebaseLinkCreator
import com.deftmove.authentication.login.LoginWithMagicTokenActivity
import com.deftmove.authentication.login.LoginWithMagicTokenPresenter
import com.deftmove.authentication.register.RegisterActivity
import com.deftmove.authentication.register.RegisterPresenter
import com.deftmove.heart.common.presenter.Presenter
import org.koin.core.qualifier.named
import org.koin.dsl.module

val authenticationModule = module {

    scope(named<RegisterActivity>()) {
        scoped<Presenter<RegisterPresenter.View>> {
            RegisterPresenter(get(), get(), get(), get())
        }
    }

    scope(named<LoginWithMagicTokenActivity>()) {
        scoped<Presenter<LoginWithMagicTokenPresenter.View>> {
            LoginWithMagicTokenPresenter(get(), get(), get())
        }
    }

    factory { FirebaseLinkCreator() }
}
