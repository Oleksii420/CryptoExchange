package com.example.cryptoexchange.presentation

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cryptoexchange.presentation.detail.CryptoDetailScreen
import com.example.cryptoexchange.presentation.list.CryptoListScreen
import com.example.cryptoexchange.presentation.list.CryptoListViewModel

@Composable
fun MainScreen(viewModel: CryptoListViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val selected = state.selectedCrypto

    if (isLandscape) {
        // ---------- Альбом: список ліворуч, деталі праворуч ----------
        Row(modifier.fillMaxSize()) {
            CryptoListScreen(
                state = state,
                onRefresh = viewModel::refresh,
                onCryptoClick = viewModel::selectCrypto,
                selectedId = selected?.id,
                listState = listState,
                modifier = Modifier.weight(0.42f)
            )
            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Box(Modifier.weight(0.58f).fillMaxHeight()) {
                if (selected != null) {
                    CryptoDetailScreen(crypto = selected, lastSyncMillis = state.lastSyncMillis, onBack = null)
                } else {
                    NoSelectionPane()
                }
            }
        }
    } else if (selected != null) {
        // ---------- Портрет, монету вибрано: деталі ----------
        BackHandler { viewModel.clearSelection() }   // системна кнопка "Назад" повертає до списку
        CryptoDetailScreen(
            crypto = selected,
            lastSyncMillis = state.lastSyncMillis,
            onBack = viewModel::clearSelection,
            modifier = modifier
        )
    } else {
        // ---------- Портрет, нічого не вибрано: список ----------
        CryptoListScreen(
            state = state,
            onRefresh = viewModel::refresh,
            onCryptoClick = viewModel::selectCrypto,
            listState = listState,
            modifier = modifier
        )
    }
}

@Composable
private fun NoSelectionPane() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Оберіть монету", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Натисніть на монету ліворуч, щоб побачити ціну, мінімум і максимум за 24 години.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}