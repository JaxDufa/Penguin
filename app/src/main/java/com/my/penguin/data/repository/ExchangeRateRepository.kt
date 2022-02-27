package com.my.penguin.data.repository

import com.my.penguin.data.Result
import com.my.penguin.data.TimestampProvider
import com.my.penguin.data.local.ExchangeRateStore
import com.my.penguin.data.model.CountryRatesResponse
import com.my.penguin.data.model.ExchangeRate
import com.my.penguin.data.model.ExchangeRates
import com.my.penguin.data.model.ExchangeRatesResponse
import com.my.penguin.data.remote.ExchangeRateService
import kotlinx.coroutines.*

private const val STORED_DATA_EXPIRATION_IN_MINUTES = 60

class ExchangeRateRepository(
    private val remote: ExchangeRateService,
    private val local: ExchangeRateStore,
    private val timestampProvider: TimestampProvider
) {

    suspend fun loadExchangeRates(): Result<ExchangeRates> {
        return withContext(Dispatchers.IO) {
            val localData = tryToLoadLocalData()
            try {
                // Since the free version of the API just updates it's data once every hour
                // We can use local stored data until it's valid
                val data = if (localData != null && localData.timestampIsNotExpired()) {
                    localData
                } else {
                    loadRemoteData(this)
                }.rates.toExchangeRate()

                Result.Success(data)
            } catch (exception: Exception) {
                Result.Error(exception)
            }
        }
    }

    private suspend fun tryToLoadLocalData(): ExchangeRatesResponse? {
        return try {
            local.loadExchangeRates()
        } catch (exception: Exception) {
            null
        }
    }

    private suspend fun loadRemoteData(coroutineScope: CoroutineScope): ExchangeRatesResponse {
        return remote.requestExchangeRates().also {
            coroutineScope.launch(SupervisorJob()) {
                local.saveExchangeRates(it)
            }
        }
    }

    private fun ExchangeRatesResponse.timestampIsNotExpired(): Boolean {
        return timestampProvider.differenceInMinutes(timestamp) < STORED_DATA_EXPIRATION_IN_MINUTES
    }

    private fun CountryRatesResponse.toExchangeRate(): ExchangeRates {
        return ExchangeRates(
            ExchangeRate(kenya),
            ExchangeRate(nigeria),
            ExchangeRate(tanzania),
            ExchangeRate(uganda)
        )
    }
}