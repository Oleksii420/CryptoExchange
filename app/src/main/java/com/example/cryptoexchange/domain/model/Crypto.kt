package com.example.cryptoexchange.domain.model


data class Crypto(
    val id: String,               // "bitcoin": стабільний ідентифікатор
    val name: String,             // "Bitcoin"
    val symbol: String,           // "BTC"
    val imageUrl: String?,        // логотип
    val priceUsd: Double?,        // null → на екрані "Немає даних"
    val rank: Int?,               // місце в рейтингу
    val high24h: Double?,         // максимум за 24 год
    val low24h: Double?,          // мінімум за 24 год
    val lastUpdatedMillis: Long?  // коли ціна оновилась на біржі
)