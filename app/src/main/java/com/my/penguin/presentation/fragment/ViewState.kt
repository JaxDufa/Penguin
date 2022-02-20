package com.my.penguin.presentation.fragment

sealed class ViewState {
    object Loading : ViewState()
    object NetworkError : ViewState()
    object UnknownError : ViewState()
    data class CurrentRate(val value: Float) : ViewState()
    data class Complete(val value: Float) : ViewState()
}