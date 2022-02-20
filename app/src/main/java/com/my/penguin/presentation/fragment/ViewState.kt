package com.my.penguin.presentation.fragment

import androidx.annotation.StringRes
import com.my.penguin.R

sealed class ViewState(val loading: Boolean = false) {
    object Loading : ViewState(true)
    data class Error(val type: ErrorType) : ViewState()
    data class CurrentRate(val value: Float) : ViewState()
    data class Complete(val value: Float) : ViewState()
}

sealed class ErrorType(@StringRes val title: Int, @StringRes val message: Int) {
    object NetworkError : ErrorType(R.string.network_error_title, R.string.network_error_message)
    object UnknownError : ErrorType(R.string.unknown_error_title, R.string.unknown_error_message)
}