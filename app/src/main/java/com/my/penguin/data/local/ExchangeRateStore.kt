package com.my.penguin.data.local

import androidx.datastore.core.DataStore
import com.my.penguin.ExchangeRatesStored
import com.my.penguin.data.model.CountryRatesResponse
import com.my.penguin.data.model.ExchangeRatesResponse
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class ExchangeRateStore(private val dataStore: DataStore<ExchangeRatesStored>) {

    suspend fun saveExchangeRates(exchangeRates: ExchangeRatesResponse) {
        dataStore.updateData { currentUserData ->
            val rates = exchangeRates.rates
            currentUserData.toBuilder()
                .setTimestamp(exchangeRates.timestamp)
                .setBase(exchangeRates.base)
                .setKenya(rates.kenya)
                .setNigeria(rates.nigeria)
                .setTanzania(rates.tanzania)
                .setUganda(rates.uganda)
                .build()
        }
    }

    suspend fun loadExchangeRates(): ExchangeRatesResponse? {
        return dataStore.data.map { exchangeRates ->
            if (exchangeRates.timestamp == 0L) return@map null
            ExchangeRatesResponse(
                exchangeRates.timestamp,
                exchangeRates.base,
                CountryRatesResponse(
                    kenya = exchangeRates.kenya,
                    nigeria = exchangeRates.nigeria,
                    tanzania = exchangeRates.tanzania,
                    uganda = exchangeRates.uganda
                )
            )
        }.firstOrNull()
    }
}