package com.example.cryptoexchange

import android.app.Application
import com.example.cryptoexchange.di.AppContainer

class CryptoApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}