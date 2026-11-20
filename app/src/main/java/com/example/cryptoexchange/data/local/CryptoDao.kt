package com.example.cryptoexchange.data.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CryptoDao {

    // Монети без рейтингу (rank = null) йдуть у кінець, решта за рейтингом 1, 2, 3...
    @Query("SELECT * FROM cryptos ORDER BY rank IS NULL, rank ASC")
    abstract fun observeAll(): Flow<List<CryptoEntity>>

    @Query("SELECT MAX(savedAtMillis) FROM cryptos")
    abstract fun observeLastSavedAt(): Flow<Long?>

    @Upsert
    abstract suspend fun upsertAll(items: List<CryptoEntity>)

    @Query("DELETE FROM cryptos")
    abstract suspend fun deleteAll()

    @Transaction
    open suspend fun replaceAll(items: List<CryptoEntity>) {
        deleteAll()
        upsertAll(items)
    }
}