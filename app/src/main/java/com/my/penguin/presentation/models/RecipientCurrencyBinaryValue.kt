package com.my.penguin.presentation.models

data class RecipientCurrencyBinaryValue(
    val prefix: String,
    val value: String
) {

    val isValidAmount: Boolean
        get() = value.contains('1')
}
