package com.my.penguin

import android.app.Application
import org.koin.core.context.GlobalContext.startKoin

class PenguinApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin()
    }

    private fun initKoin() {
        startKoin {
            modules(defaultModule)
        }
    }
}