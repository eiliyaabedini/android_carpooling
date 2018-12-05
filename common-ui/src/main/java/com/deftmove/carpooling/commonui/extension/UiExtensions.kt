package com.deftmove.carpooling.commonui.extension

import com.deftmove.carpooling.interfaces.authentication.model.UserGender
import com.deftmove.heart.common.ui.R
import com.deftmove.heart.interfaces.common.TextUtils

fun UserGender.getGenderLabelText(textUtils: TextUtils): String {
    return when (this) {
        UserGender.MALE -> textUtils.getString(R.string.common_gender_male_hint)
        UserGender.FEMALE -> textUtils.getString(R.string.common_gender_female_hint)
        UserGender.UNKNOWN -> textUtils.getString(R.string.common_gender_unknown_hint)
    }
}
