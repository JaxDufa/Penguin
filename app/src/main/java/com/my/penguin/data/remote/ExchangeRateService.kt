package com.my.penguin.data.remote

import com.my.penguin.data.model.ExchangeRatesResponse
import retrofit2.http.GET

interface ExchangeRateService {

    @GET("latest.json")
    suspend fun requestExchangeRates(): ExchangeRatesResponse
}