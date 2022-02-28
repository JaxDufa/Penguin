package com.my.penguin.presentation.fragment

import com.my.penguin.R
import com.my.penguin.presentation.DialogData
import com.my.penguin.presentation.OnClick
import com.my.penguin.presentation.StringResData
import com.my.penguin.presentation.model.Country
import com.my.penguin.presentation.model.Transaction

sealed class ViewState(val loading: Boolean = false) {
    object Loading : ViewState(true)
    data class GeneralError(val dialog: ErrorDialog) : ViewState()
    data class InputFieldError(val inputFieldStatus: InputFieldsStatus) : ViewState()

    data class Initial(val countriesName: List<String>) : ViewState()
    data class Default(val country: Country) : ViewState()
    data class Finish(val dialog: TransactionDialog) : ViewState()
}

data class InputFieldsStatus(
    val isValidFirstName: Boolean = false,
    val isValidLastName: Boolean = false,
    val isValidPhoneNumber: Boolean = false
) {
    val isAnyFieldInvalid: Boolean
        get() = !isValidFirstName || !isValidLastName || !isValidPhoneNumber
}

sealed class ErrorDialog(val data: DialogData) {
    data class NetworkError(val onClickPositive: OnClick) : ErrorDialog(
        DialogData(
            R.string.network_error_title,
            StringResData(R.string.network_error_message),
            DialogData.DialogButton(R.string.error_positive_button, onClickPositive)
        )
    )

    data class UnknownError(val onClickPositive: OnClick) : ErrorDialog(
        DialogData(
            R.string.unknown_error_title,
            StringResData(R.string.unknown_error_message),
            DialogData.DialogButton(R.string.error_positive_button, onClickPositive)
        )
    )
}

sealed class TransactionDialog(val data: DialogData) {
    data class Confirm(
        private val transaction: Transaction,
        val onClickPositive: OnClick,
        val onClickNegative: OnClick
    ) :
        TransactionDialog(
            DialogData(
                R.string.confirm_title,
                StringResData(
                    R.string.confirm_message,
                    listOf(transaction.amount, transaction.recipientPhone)
                ),
                DialogData.DialogButton(R.string.confirm_positive_action, onClickPositive),
                DialogData.DialogButton(R.string.confirm_negative_action, onClickNegative)
            )
        )

    data class Complete(private val transaction: Transaction, val onClickPositive: OnClick) :
        TransactionDialog(
            DialogData(
                R.string.transaction_complete_title,
                StringResData(
                    R.string.transaction_complete_message,
                    listOf(transaction.amount, transaction.recipientPhone)
                ),
                DialogData.DialogButton(R.string.transaction_positive_action, onClickPositive)
            )
        )
}