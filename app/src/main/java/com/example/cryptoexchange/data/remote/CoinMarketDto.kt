package com.example.cryptoexchange.data.remote

import com.google.gson.annotations.SerializedName

// Одна монета так, як її присилає CoinGecko.
// @SerializedName каже: "у JSON поле називається отак".
data class CoinMarketDto(
    @SerializedName("id") val id: String?,
    @SerializedName("symbol") val symbol: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("current_price") val currentPrice: Double?,
    @SerializedName("market_cap_rank") val marketCapRank: Int?,
    @SerializedName("high_24h") val high24h: Double?,
    @SerializedName("low_24h") val low24h: Double?,
    @SerializedName("last_updated") val lastUpdated: String?
)