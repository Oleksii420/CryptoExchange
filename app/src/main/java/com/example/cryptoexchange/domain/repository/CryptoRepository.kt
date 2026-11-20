package com.example.cryptoexchange.domain.repository

import com.example.cryptoexchange.domain.model.Crypto
import com.example.cryptoexchange.domain.model.RefreshResult
import kotlinx.coroutines.flow.Flow


interface CryptoRepository {
    fun observeCryptos(): Flow<List<Crypto>>
    fun observeLastSyncTime(): Flow<Long?>
    suspend fun refresh(): RefreshResult
}