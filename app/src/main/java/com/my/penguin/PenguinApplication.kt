package com.my.penguin

import android.app.Application
import com.my.penguin.data.di.apiModule
import org.koin.core.context.GlobalContext.startKoin

class PenguinApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin()
    }

    private fun initKoin() {
        startKoin {
            modules(
                mainModule,
                apiModule
            )
        }
    }
}