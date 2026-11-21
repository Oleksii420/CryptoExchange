package com.example.cryptoexchange.data.repository

import com.example.cryptoexchange.data.local.CryptoDao
import com.example.cryptoexchange.data.mapper.toDomain
import com.example.cryptoexchange.data.mapper.toEntity
import com.example.cryptoexchange.data.remote.CoinGeckoApi
import com.example.cryptoexchange.domain.model.Crypto
import com.example.cryptoexchange.domain.model.RefreshResult
import com.example.cryptoexchange.domain.repository.CryptoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException

class CryptoRepositoryImpl(
    private val api: CoinGeckoApi,
    private val dao: CryptoDao
) : CryptoRepository {

    override fun observeCryptos(): Flow<List<Crypto>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeLastSyncTime(): Flow<Long?> = dao.observeLastSavedAt()

    override suspend fun refresh(): RefreshResult {
        val response = try {
            api.getTopCoins()
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            return RefreshResult.NoInternet
        } catch (e: Exception) {
            return RefreshResult.BadResponse
        }

        if (!response.isSuccessful) {
            return when (response.code()) {
                401, 403 -> RefreshResult.InvalidApiKey
                429 -> RefreshResult.RateLimited
                else -> RefreshResult.ServerError(response.code())
            }
        }

        val body = response.body() ?: return RefreshResult.BadResponse
        val now = System.currentTimeMillis()
        val entities = body.mapNotNull { it.toEntity(now) }
        if (entities.isEmpty()) return RefreshResult.BadResponse

        dao.replaceAll(entities)
        return RefreshResult.Success(entities.size)
    }
}