package com.example.cryptoexchange.di

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.example.cryptoexchange.data.local.CryptoDatabase
import com.example.cryptoexchange.data.remote.CoinGeckoNetwork
import com.example.cryptoexchange.data.repository.CryptoRepositoryImpl
import com.example.cryptoexchange.domain.repository.CryptoRepository
import com.example.cryptoexchange.domain.usecase.ObserveCryptosUseCase
import com.example.cryptoexchange.domain.usecase.ObserveLastSyncTimeUseCase
import com.example.cryptoexchange.domain.usecase.RefreshCryptosUseCase

class AppContainer(context: Context) {

    private val database: CryptoDatabase = Room.databaseBuilder(
        context.applicationContext,
        CryptoDatabase::class.java,
        "crypto.db"
    )
        .setDriver(AndroidSQLiteDriver())
        .build()

    private val repository: CryptoRepository = CryptoRepositoryImpl(
        api = CoinGeckoNetwork.api,
        dao = database.cryptoDao()
    )

    val observeCryptos = ObserveCryptosUseCase(repository)
    val observeLastSyncTime = ObserveLastSyncTimeUseCase(repository)
    val refreshCryptos = RefreshCryptosUseCase(repository)
}