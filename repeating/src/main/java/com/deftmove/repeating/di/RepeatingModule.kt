package com.deftmove.repeating.di

import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.repeating.RepeatingActivity
import com.deftmove.repeating.RepeatingActivityPresenter
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repeatingModule = module {
    scope(named<RepeatingActivity>()) {
        scoped<Presenter<RepeatingActivityPresenter.View>> {
            RepeatingActivityPresenter(get())
        }
    }
}
