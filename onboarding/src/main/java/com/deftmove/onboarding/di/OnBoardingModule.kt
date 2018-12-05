package com.deftmove.onboarding.di

import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.onboarding.OnBoardingActivity
import com.deftmove.onboarding.OnBoardingPresenter
import org.koin.core.qualifier.named
import org.koin.dsl.module

val onBoardingModule = module {
    scope(named<OnBoardingActivity>()) {
        scoped<Presenter<OnBoardingPresenter.View>> {
            OnBoardingPresenter(get(), get(), get())
        }
    }
}
