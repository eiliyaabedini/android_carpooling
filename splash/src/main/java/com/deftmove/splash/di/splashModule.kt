package com.deftmove.splash.di

import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.splash.SplashActivity
import com.deftmove.splash.SplashPresenter
import org.koin.core.qualifier.named
import org.koin.dsl.module

val splashModule = module {
    scope(named<SplashActivity>()) {
        scoped<Presenter<SplashPresenter.View>> {
            SplashPresenter(get(), get(), get())
        }
    }
}
