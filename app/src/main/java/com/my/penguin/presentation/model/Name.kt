package com.my.penguin.presentation.model

data class Name(private val firstName: String, private val lastName: String) {

    val fullName: String
        get() = "$firstName $lastName"
}