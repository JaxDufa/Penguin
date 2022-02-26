package com.my.penguin.presentation.model

import com.my.penguin.data.model.ExchangeRate

sealed class Country(
    val name: String,
    val currencyPrefix: String,
    val phonePrefix: String,
    val phoneNumberDigits: Int,
    var exchangeRate: ExchangeRate = ExchangeRate()
) {
    object Kenya : Country("Kenya", "KES", "+254", 9)
    object Nigeria : Country("Nigeria", "NGN", "+234", 7)
    object Tanzania : Country("Tanzania", "TZS", "+255", 9)
    object Uganda : Country("Uganda", "UGX", "+256", 7)
}