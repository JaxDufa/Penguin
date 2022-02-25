package com.my.penguin.data

import com.my.penguin.data.model.ExchangeRatesResponse
import retrofit2.Response
import retrofit2.http.GET

interface ExchangeRateService {

    @GET("latest.json")
    suspend fun getExchangeRates(): ExchangeRatesResponse
}