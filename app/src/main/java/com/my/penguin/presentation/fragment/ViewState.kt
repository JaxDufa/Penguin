package com.my.penguin.presentation.fragment

import androidx.annotation.StringRes
import com.my.penguin.R
import com.my.penguin.presentation.models.Country
import com.my.penguin.presentation.models.Transaction

sealed class ViewState(val loading: Boolean = false) {
    object Loading : ViewState(true)
    data class GeneralError(val type: ErrorType) : ViewState()
    data class InputFieldError(val inputFieldStatus: InputFieldsStatus) : ViewState()

    data class Initial(val countriesName: List<String>) : ViewState()
    data class Default(val country: Country) : ViewState()
    data class Confirm(val transaction: Transaction) : ViewState()
    data class Complete(val transaction: Transaction) : ViewState()
}

data class InputFieldsStatus(
    val isValidFirstName: Boolean = false,
    val isValidLastName: Boolean = false,
    val isValidPhoneNumber: Boolean = false
) {
    val isAnyFieldInvalid: Boolean
        get() = !isValidFirstName || !isValidLastName || !isValidPhoneNumber
}

sealed class ErrorType(@StringRes val title: Int, @StringRes val message: Int) {
    object NetworkError : ErrorType(R.string.network_error_title, R.string.network_error_message)
    object UnknownError : ErrorType(R.string.unknown_error_title, R.string.unknown_error_message)
}