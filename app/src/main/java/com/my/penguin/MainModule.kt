package com.my.penguin

import android.content.Context
import android.net.ConnectivityManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val mainModule = module {
    factory { provideConnectivityManager(androidContext()) }
    single { NetworkProvider(get()) }
}

fun provideConnectivityManager(context: Context): ConnectivityManager {
    return context.getSystemService(ConnectivityManager::class.java)
}