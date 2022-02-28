package com.my.penguin.presentation

import android.content.Context
import androidx.annotation.StringRes

data class StringResData(
    @StringRes private val resId: Int,
    private val args: List<Any>? = null
) {
    fun getString(context: Context): String {
        return if (args.isNullOrEmpty()) {
            context.getString(resId)
        } else {
            context.getString(resId, *args.toTypedArray())
        }
    }
}