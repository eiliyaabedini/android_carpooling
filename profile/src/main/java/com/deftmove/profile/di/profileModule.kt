package com.deftmove.profile.di

import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.profile.edit.ProfileEditActivity
import com.deftmove.profile.edit.ProfileEditPresenter
import com.deftmove.profile.profile.ProfileActivity
import com.deftmove.profile.profile.ProfilePresenter
import com.deftmove.profile.publicprofile.PublicProfileActivity
import com.deftmove.profile.publicprofile.PublicProfilePresenter
import org.koin.core.qualifier.named
import org.koin.dsl.module

val profileModule = module {

    scope(named<ProfileActivity>()) {
        scoped<Presenter<ProfilePresenter.View>> {
            ProfilePresenter(get(), get(), get(), get(), get())
        }
    }

    scope(named<ProfileEditActivity>()) {
        scoped<Presenter<ProfileEditPresenter.View>> {
            ProfileEditPresenter(get(), get(), get())
        }
    }

    scope(named<PublicProfileActivity>()) {
        scoped<Presenter<PublicProfilePresenter.View>> {
            PublicProfilePresenter(get(), get())
        }
    }
}
