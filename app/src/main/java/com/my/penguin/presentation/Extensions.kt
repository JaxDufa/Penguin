package com.my.penguin.presentation

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Context.showDialog(dialogData: DialogData) {
    with(dialogData) {
        MaterialAlertDialogBuilder(this@showDialog)
            .setCancelable(false)
            .setTitle(title)
            .setMessage(message.getString(this@showDialog))
            .setPositiveButton(getString(positiveButton.button)) { dialog, _ ->
                positiveButton.onClick()
                dialog.dismiss()
            }.apply {
                negativeButton?.let {
                    setNegativeButton(getString(it.button)) { dialog, _ ->
                        it.onClick()
                        dialog.dismiss()
                    }
                }
            }.show()
    }
}

fun Activity.hideKeyboard(view: View) {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}