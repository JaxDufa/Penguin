package com.my.penguin.presentation.model

import android.telephony.PhoneNumberUtils

data class CountryPhone(private val prefix: String, private val phone: String) {

    val fullPhoneNumber: String
        get() = "$prefix $phone"

    fun isValid(phoneNumberDigits: Int): Boolean {
        val hasExpectedNumberOfDigits = phone.length == phoneNumberDigits
        val isValidPhoneNumber =
            PhoneNumberUtils.isGlobalPhoneNumber(fullPhoneNumber.replace(" ", ""))
        return hasExpectedNumberOfDigits && isValidPhoneNumber
    }
}