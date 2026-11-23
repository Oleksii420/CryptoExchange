package com.example.cryptoexchange

import com.example.cryptoexchange.data.local.CryptoDao
import com.example.cryptoexchange.data.local.CryptoEntity
import com.example.cryptoexchange.data.remote.CoinGeckoApi
import com.example.cryptoexchange.data.remote.CoinMarketDto
import com.example.cryptoexchange.data.repository.CryptoRepositoryImpl
import com.example.cryptoexchange.domain.model.RefreshResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.Response
import java.io.IOException

// ---------- Підробки замість справжньої бази і справжнього сервера ----------
class FakeCryptoDao : CryptoDao() {
    private val table = MutableStateFlow<List<CryptoEntity>>(emptyList())
    val rows: List<CryptoEntity> get() = table.value

    override fun observeAll(): Flow<List<CryptoEntity>> =
        table.map { list -> list.sortedBy { it.rank ?: Int.MAX_VALUE } }

    override fun observeLastSavedAt(): Flow<Long?> =
        table.map { list -> list.maxOfOrNull { it.savedAtMillis } }

    override suspend fun upsertAll(items: List<CryptoEntity>) {
        val byId = table.value.associateBy { it.id }.toMutableMap()
        items.forEach { byId[it.id] = it }
        table.value = byId.values.toList()
    }

    override suspend fun deleteAll() {
        table.value = emptyList()
    }
}

class FakeCoinGeckoApi : CoinGeckoApi {
    var answer: () -> Response<List<CoinMarketDto>> = { Response.success(emptyList()) }

    override suspend fun getTopCoins(
        currency: String,
        order: String,
        perPage: Int,
        page: Int,
        precision: String
    ): Response<List<CoinMarketDto>> = answer()
}

// ---------- Самі тести ----------

class CryptoRepositoryTest {

    private val api = FakeCoinGeckoApi()
    private val dao = FakeCryptoDao()
    private val repository = CryptoRepositoryImpl(api = api, dao = dao)

    private fun coin(id: String, rank: Int, price: Double? = 1.0) = CoinMarketDto(
        id = id, symbol = id, name = id, image = null, currentPrice = price,
        marketCapRank = rank, high24h = null, low24h = null, lastUpdated = null
    )

    private fun serverReturns(vararg coins: CoinMarketDto) {
        api.answer = { Response.success(coins.toList()) }
    }

    private fun serverFails(code: Int) {
        api.answer = { Response.error(code, "".toResponseBody()) }
    }

    @Test
    fun `успішне оновлення записує монети в базу за рейтингом`() = runBlocking<Unit> {
        serverReturns(coin("ethereum", 2), coin("bitcoin", 1), coin("tether", 3))
        assertEquals(RefreshResult.Success(3), repository.refresh())
        assertEquals(listOf("bitcoin", "ethereum", "tether"), repository.observeCryptos().first().map { it.id })
    }

    @Test
    fun `повторне оновлення не створює дублікатів`() = runBlocking<Unit> {
        serverReturns(coin("bitcoin", 1), coin("ethereum", 2))
        repository.refresh()
        repository.refresh()
        assertEquals(2, dao.rows.size)
    }

    @Test
    fun `новий рейтинг замінює старий, вибулі монети зникають`() = runBlocking<Unit> {
        serverReturns(coin("bitcoin", 1), coin("ethereum", 2), coin("tether", 3))
        repository.refresh()

        serverReturns(coin("ethereum", 1), coin("bitcoin", 2), coin("solana", 3))   // tether випав з топу
        repository.refresh()

        assertEquals(listOf("ethereum", "bitcoin", "solana"), repository.observeCryptos().first().map { it.id })
    }

    @Test
    fun `без інтернету старі дані залишаються`() = runBlocking<Unit> {
        serverReturns(coin("bitcoin", 1), coin("ethereum", 2))
        repository.refresh()
        val before = dao.rows

        api.answer = { throw IOException("Немає мережі") }

        assertEquals(RefreshResult.NoInternet, repository.refresh())
        assertEquals(before, dao.rows)
    }

    @Test
    fun `помилки ключа, ліміту і сервера розрізняються, база ціла`() = runBlocking<Unit> {
        serverReturns(coin("bitcoin", 1))
        repository.refresh()
        val before = dao.rows

        serverFails(401)
        assertEquals(RefreshResult.InvalidApiKey, repository.refresh())
        serverFails(429)
        assertEquals(RefreshResult.RateLimited, repository.refresh())
        serverFails(503)
        assertEquals(RefreshResult.ServerError(503), repository.refresh())

        assertEquals(before, dao.rows)
    }

    @Test
    fun `порожня відповідь не стирає базу`() = runBlocking<Unit> {
        serverReturns(coin("bitcoin", 1))
        repository.refresh()
        val before = dao.rows

        serverReturns()

        assertEquals(RefreshResult.BadResponse, repository.refresh())
        assertEquals(before, dao.rows)
    }

    @Test
    fun `монета без ціни зберігається з null`() = runBlocking<Unit> {
        serverReturns(coin("bitcoin", 1), coin("mystery", 2, price = null))
        repository.refresh()
        val mystery = repository.observeCryptos().first().first { it.id == "mystery" }
        assertNull(mystery.priceUsd)
    }
}