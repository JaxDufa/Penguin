package com.my.penguin.data.local

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import com.my.penguin.ExchangeRatesStored
import com.my.penguin.ExchangeRatesStored.parseFrom
import java.io.InputStream
import java.io.OutputStream

object ExchangeRateSerializer : Serializer<ExchangeRatesStored> {

    override val defaultValue: ExchangeRatesStored = ExchangeRatesStored.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ExchangeRatesStored {
        try {
            return parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: ExchangeRatesStored, output: OutputStream) = t.writeTo(output)
}

val Context.exchangeRateDataStore: DataStore<ExchangeRatesStored> by dataStore(
    fileName = "exchange.pb",
    serializer = ExchangeRateSerializer
)