package com.deftmove.carpooling.interfaces.common.data

sealed class IconsType {
    object HomeScreenActionBar : IconsType()
    data class ProfilePublicScreenActionBar(val userId: String) : IconsType()
}
