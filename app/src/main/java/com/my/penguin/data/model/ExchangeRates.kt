package com.my.penguin.data.model

data class ExchangeRates(
    val kenya: ExchangeRate,
    val nigeria: ExchangeRate,
    val tanzania: ExchangeRate,
    val uganda: ExchangeRate
)

@JvmInline
value class ExchangeRate(private val value: Float = 0.0f) {
    fun apply(amount: Long): Float {
        return amount * value
    }
}