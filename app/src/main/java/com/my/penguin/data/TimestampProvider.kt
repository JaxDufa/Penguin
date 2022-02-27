package com.my.penguin.data

import java.util.concurrent.TimeUnit

class TimestampProvider {
    private val timestampInSeconds: Long
        get() = System.currentTimeMillis() / 1000

    fun differenceInMinutes(timestamp: Long) =
        TimeUnit.SECONDS.toMinutes(timestampInSeconds - timestamp)
}