package com.example.cryptoexchange.data.mapper

import com.example.cryptoexchange.data.local.CryptoEntity
import com.example.cryptoexchange.data.remote.CoinMarketDto
import com.example.cryptoexchange.domain.model.Crypto
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun CoinMarketDto.toEntity(savedAtMillis: Long): CryptoEntity? {
    val safeId = id.cleanOrNull() ?: return null

    return CryptoEntity(
        id = safeId,
        name = name.cleanOrNull() ?: safeId,
        symbol = (symbol.cleanOrNull() ?: safeId).uppercase(),
        imageUrl = image.cleanOrNull(),
        priceUsd = currentPrice.validPriceOrNull(),
        rank = marketCapRank?.takeIf { it > 0 },
        high24h = high24h.validPriceOrNull(),
        low24h = low24h.validPriceOrNull(),
        lastUpdatedMillis = parseIsoMillis(lastUpdated),
        savedAtMillis = savedAtMillis
    )
}


fun CryptoEntity.toDomain(): Crypto = Crypto(
    id = id,
    name = name,
    symbol = symbol,
    imageUrl = imageUrl,
    priceUsd = priceUsd,
    rank = rank,
    high24h = high24h,
    low24h = low24h,
    lastUpdatedMillis = lastUpdatedMillis
)
private fun String?.cleanOrNull(): String? =
    this?.trim()?.takeIf { it.isNotEmpty() }

private fun Double?.validPriceOrNull(): Double? =
    this?.takeIf { it.isFinite() && it >= 0 }

private fun parseIsoMillis(text: String?): Long? {
    if (text.isNullOrBlank()) return null
    val patterns = listOf("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'")
    for (pattern in patterns) {
        val format = SimpleDateFormat(pattern, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
            isLenient = false
        }
        try {
            return format.parse(text)?.time
        } catch (e: ParseException) {
        }
    }
    return null
}