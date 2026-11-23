package com.example.cryptoexchange.presentation.list

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptoexchange.CryptoApp
import com.example.cryptoexchange.domain.model.Crypto
import com.example.cryptoexchange.sync.CryptoSyncScheduler
import com.example.cryptoexchange.sync.SyncStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CryptoListUiState(
    val isLoading: Boolean = true,
    val cryptos: List<Crypto> = emptyList(),
    val lastSyncMillis: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.Idle,
    val selectedId: String? = null
) {
    val selectedCrypto: Crypto?
        get() = cryptos.firstOrNull { it.id == selectedId }
}

class CryptoListViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as CryptoApp).container

    private val selectedId = MutableStateFlow<String?>(null)

    init {
        Log.d(TAG, "ViewModel створено")
    }

    val uiState: StateFlow<CryptoListUiState> = combine(
        container.observeCryptos(),
        container.observeLastSyncTime(),
        CryptoSyncScheduler.observeManualStatus(application),
        selectedId
    ) { cryptos, lastSync, status, selected ->
        CryptoListUiState(
            isLoading = false,
            cryptos = cryptos,
            lastSyncMillis = lastSync,
            syncStatus = status,
            selectedId = selected
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CryptoListUiState()
    )

    fun refresh() {
        CryptoSyncScheduler.refreshNow(getApplication())
    }

    fun selectCrypto(id: String) {
        selectedId.value = id
    }

    fun clearSelection() {
        selectedId.value = null
    }

    override fun onCleared() {
        Log.d(TAG, "ViewModel знищено")
    }

    private companion object {
        const val TAG = "CryptoVM"
    }
}