package com.my.penguin

import android.app.Application
import org.koin.core.context.GlobalContext.startKoin

class KoinTestApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(emptyList())
        }
    }
}