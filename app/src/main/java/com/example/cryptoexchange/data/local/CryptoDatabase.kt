package com.example.cryptoexchange.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(entities = [CryptoEntity::class], version = 1, exportSchema = false)
abstract class CryptoDatabase : RoomDatabase() {
    abstract fun cryptoDao(): CryptoDao
}