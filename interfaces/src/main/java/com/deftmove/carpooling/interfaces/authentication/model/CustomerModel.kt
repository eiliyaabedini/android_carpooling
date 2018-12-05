package com.deftmove.carpooling.interfaces.authentication.model

import com.deftmove.heart.interfaces.common.model.Money

data class CustomerModel(
    val currencyIsoCode: String,
    val farePerKm: Money,
    val name: String
)
