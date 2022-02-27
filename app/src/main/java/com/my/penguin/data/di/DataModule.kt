package com.my.penguin.data.di

import android.content.Context
import com.my.penguin.BuildConfig
import com.my.penguin.data.TimestampProvider
import com.my.penguin.data.local.ExchangeRateStore
import com.my.penguin.data.local.exchangeRateDataStore
import com.my.penguin.data.remote.ExchangeRateService
import com.my.penguin.data.repository.ExchangeRateRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val BASE_URL = BuildConfig.API_URL
private const val APP_ID_QUERY = "app_id"

val dataModule = module {
    single { provideOkHttpClient() }
    single { provideRetrofit(get()) }
    single { provideApiService(get()) }

    single { provideStore(androidContext()) }

    single { TimestampProvider() }
    single { ExchangeRateStore(get()) }
    single { ExchangeRateRepository(get(), get(), get()) }
}

private fun provideStore(context: Context) = context.exchangeRateDataStore

private fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(AuthenticationInterceptor())
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()
}

private fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create())
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .build()
}

private fun provideApiService(retrofit: Retrofit) = retrofit.create(ExchangeRateService::class.java)

private class AuthenticationInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var original = chain.request()

        val url = original
            .url
            .newBuilder()
            .addQueryParameter(APP_ID_QUERY, BuildConfig.API_KEY)
            .build()

        original = original.newBuilder()
            .url(url)
            .build()

        return chain.proceed(original)
    }
}