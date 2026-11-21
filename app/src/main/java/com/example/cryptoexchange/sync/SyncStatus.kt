package com.example.cryptoexchange.sync

// Що зараз відбувається з кнопкою "Оновити"
sealed interface SyncStatus {
    data object Idle : SyncStatus                        // нічого не відбувається
    data object WaitingForNetwork : SyncStatus           // у черзі, чекає інтернет
    data object Running : SyncStatus                     // оновлюється прямо зараз
    data object Retrying : SyncStatus                    // була помилка, скоро повтор
    data class Succeeded(val message: String) : SyncStatus
    data class Failed(val message: String) : SyncStatus
}