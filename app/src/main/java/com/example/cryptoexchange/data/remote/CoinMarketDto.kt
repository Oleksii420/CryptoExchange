package com.example.cryptoexchange.data.remote

import com.google.gson.annotations.SerializedName

// Одна монета так, як її присилає CoinGecko.
// @SerializedName каже: "у JSON поле називається отак".
data class CoinMarketDto(
    @SerializedName("id") val id: String?,                       // "bitcoin"
    @SerializedName("symbol") val symbol: String?,               // "btc"
    @SerializedName("name") val name: String?,                   // "Bitcoin"
    @SerializedName("image") val image: String?,                 // адреса логотипа
    @SerializedName("current_price") val currentPrice: Double?,  // ціна в USD
    @SerializedName("market_cap_rank") val marketCapRank: Int?,  // місце в рейтингу
    @SerializedName("high_24h") val high24h: Double?,            // максимум за 24 год
    @SerializedName("low_24h") val low24h: Double?,              // мінімум за 24 год
    @SerializedName("last_updated") val lastUpdated: String?     // "2026-11-20T12:30:00.000Z"
)