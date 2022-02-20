package com.my.penguin.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExchangeRatesResponse(
    val timestamp: Long,
    val base: String,
    val rates: CountryRatesResponse
)

@JsonClass(generateAdapter = true)
data class CountryRatesResponse(
    @Json(name = "KES") val kenya: Float,
    @Json(name = "NGN") val nigeria: Float,
    @Json(name = "TZS") val tanzania: Float,
    @Json(name = "UGX") val uganda: Float
)