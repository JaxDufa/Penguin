package com.my.penguin.data

import com.my.penguin.data.model.CountryRatesResponse
import com.my.penguin.data.model.ExchangeRates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExchangeRateRepository(private val service: ExchangeRateService) {

    suspend fun loadExchangeRates(): Result<ExchangeRates> {
        return withContext(Dispatchers.IO) {
            try {
                val data = service.getExchangeRates().rates.toExchangeRate()
                Result.Success(data)
            } catch (exception: Exception) {
                Result.Error(exception)
            }
        }
    }

    private fun CountryRatesResponse.toExchangeRate(): ExchangeRates {
        return ExchangeRates(
            kenya,
            nigeria,
            tanzania,
            uganda
        )
    }
}