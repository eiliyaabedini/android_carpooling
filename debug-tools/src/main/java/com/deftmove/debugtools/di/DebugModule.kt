package com.deftmove.debugtools.di

import com.deftmove.debugtools.accountswitch.AccountSwitcherActivity
import com.deftmove.debugtools.accountswitch.AccountSwitcherPresenter
import com.deftmove.debugtools.matchmaker.MatchMakerActivity
import com.deftmove.debugtools.matchmaker.MatchMakerPresenter
import com.deftmove.heart.common.presenter.Presenter
import org.koin.core.qualifier.named
import org.koin.dsl.module

val debugModule = module {

    scope(named<MatchMakerActivity>()) {
        scoped<Presenter<MatchMakerPresenter.View>> {
            MatchMakerPresenter(get(), get(), get(), get(), get(), get(), get())
        }
    }

    scope(named<AccountSwitcherActivity>()) {
        scoped<Presenter<AccountSwitcherPresenter.View>> {
            AccountSwitcherPresenter(get(), get(), get(), get())
        }
    }
}
