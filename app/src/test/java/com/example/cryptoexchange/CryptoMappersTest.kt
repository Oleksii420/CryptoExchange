package com.example.cryptoexchange

import com.example.cryptoexchange.data.mapper.toEntity
import com.example.cryptoexchange.data.remote.CoinMarketDto
import com.example.cryptoexchange.presentation.format.formatPrice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CryptoMappersTest {

    private fun dto(
        id: String? = "bitcoin",
        symbol: String? = "btc",
        image: String? = "https://example.com/btc.png",
        price: Double? = 61556.63,
        lastUpdated: String? = "2026-11-20T12:30:00.000Z"
    ) = CoinMarketDto(
        id = id,
        symbol = symbol,
        name = "Bitcoin",
        image = image,
        currentPrice = price,
        marketCapRank = 1,
        high24h = 62870.40,
        low24h = 60102.15,
        lastUpdated = lastUpdated
    )

    @Test
    fun `відсутня ціна стає null, а не 0`() {
        val entity = dto(price = null).toEntity(savedAtMillis = 1L)!!
        assertNull(entity.priceUsd)
    }

    @Test
    fun `відʼємна ціна вважається відсутньою`() {
        val entity = dto(price = -5.0).toEntity(savedAtMillis = 1L)!!
        assertNull(entity.priceUsd)
    }

    @Test
    fun `відсутнє або порожнє зображення стає null`() {
        assertNull(dto(image = null).toEntity(1L)!!.imageUrl)
        assertNull(dto(image = "   ").toEntity(1L)!!.imageUrl)
    }

    @Test
    fun `монета без id відкидається`() {
        assertNull(dto(id = null).toEntity(1L))
    }

    @Test
    fun `символ перетворюється на великі літери`() {
        assertEquals("BTC", dto(symbol = "btc").toEntity(1L)!!.symbol)
    }

    @Test
    fun `час з біржі розбирається з мілісекундами і без них`() {
        assertEquals(1795177800000L, dto(lastUpdated = "2026-11-20T12:30:00.000Z").toEntity(1L)!!.lastUpdatedMillis)
        assertEquals(1795177800000L, dto(lastUpdated = "2026-11-20T12:30:00Z").toEntity(1L)!!.lastUpdatedMillis)
        assertNull(dto(lastUpdated = "зламаний час").toEntity(1L)!!.lastUpdatedMillis)
    }

    @Test
    fun `малі ціни не округлюються до нуля`() {
        assertEquals("\$0.00001234", formatPrice(0.00001234))
        assertEquals("\$61,556.63", formatPrice(61556.63))
        assertNull(formatPrice(null))
    }
}