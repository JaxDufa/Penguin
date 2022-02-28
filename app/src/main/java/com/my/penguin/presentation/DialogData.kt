package com.my.penguin.presentation

import androidx.annotation.StringRes

typealias OnClick = () -> Unit

data class DialogData(
    @StringRes val title: Int,
    val message: StringResData,
    val positiveButton: DialogButton,
    val negativeButton: DialogButton? = null
) {
    data class DialogButton(
        @StringRes val button: Int,
        val onClick: OnClick
    )
}