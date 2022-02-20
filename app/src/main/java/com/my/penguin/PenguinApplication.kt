package com.my.penguin

import android.app.Application
import com.my.penguin.data.di.dataModule
import com.my.penguin.presentation.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class PenguinApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidContext(this@PenguinApplication)
            modules(
                mainModule,
                viewModelModule,
                dataModule
            )
        }
    }
}