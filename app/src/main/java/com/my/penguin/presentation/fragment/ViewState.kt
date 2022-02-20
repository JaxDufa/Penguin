package com.my.penguin.presentation.fragment

import androidx.annotation.StringRes
import com.my.penguin.R
import com.my.penguin.presentation.models.Country
import com.my.penguin.presentation.models.Transaction

sealed class ViewState(val loading: Boolean = false) {
    object Loading : ViewState(true)
    data class Error(val type: ErrorType) : ViewState()
    data class InputFieldError(
        val firstName: Boolean,
        val lastName: Boolean,
        val phoneNumber: Boolean
    ) : ViewState()

    data class Initial(val countriesName: List<String>) : ViewState()
    data class Default(val country: Country) : ViewState()
    data class Confirm(val transaction: Transaction) : ViewState()
    data class Complete(val transaction: Transaction) : ViewState()
}

sealed class ErrorType(@StringRes val title: Int, @StringRes val message: Int) {
    object NetworkError : ErrorType(R.string.network_error_title, R.string.network_error_message)
    object UnknownError : ErrorType(R.string.unknown_error_title, R.string.unknown_error_message)
}