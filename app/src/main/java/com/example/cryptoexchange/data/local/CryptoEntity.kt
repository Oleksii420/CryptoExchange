package com.example.cryptoexchange.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "cryptos")
data class CryptoEntity(
    @PrimaryKey val id: String,
    val name: String,
    val symbol: String,
    val imageUrl: String?,
    val priceUsd: Double?,
    val rank: Int?,
    val high24h: Double?,
    val low24h: Double?,
    val lastUpdatedMillis: Long?,   // час з біржі
    val savedAtMillis: Long         // коли МИ зберегли
)