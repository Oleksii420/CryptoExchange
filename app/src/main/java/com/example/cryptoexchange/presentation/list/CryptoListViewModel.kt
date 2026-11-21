package com.example.cryptoexchange.presentation.list

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptoexchange.CryptoApp
import com.example.cryptoexchange.domain.model.Crypto
import com.example.cryptoexchange.sync.CryptoSyncScheduler
import com.example.cryptoexchange.sync.SyncStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// Усе, що потрібно екрану, в одному об'єкті
data class CryptoListUiState(
    val isLoading: Boolean = true,              // ще читаємо базу
    val cryptos: List<Crypto> = emptyList(),
    val lastSyncMillis: Long? = null,           // коли востаннє зберегли дані
    val syncStatus: SyncStatus = SyncStatus.Idle
)

class CryptoListViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as CryptoApp).container

    init {
        Log.d(TAG, "ViewModel створено")
    }

    // Три "річки" зливаються в одну: змінилась будь-яка, отже з'являється новий стан
    val uiState: StateFlow<CryptoListUiState> = combine(
        container.observeCryptos(),
        container.observeLastSyncTime(),
        CryptoSyncScheduler.observeManualStatus(application)
    ) { cryptos, lastSync, status ->
        CryptoListUiState(
            isLoading = false,
            cryptos = cryptos,
            lastSyncMillis = lastSync,
            syncStatus = status
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CryptoListUiState()      // поки нічого не прочитали: isLoading = true
    )

    // Кнопка "Оновити"
    fun refresh() {
        CryptoSyncScheduler.refreshNow(getApplication())
    }

    override fun onCleared() {
        Log.d(TAG, "ViewModel знищено")
    }

    private companion object {
        const val TAG = "CryptoVM"
    }
}