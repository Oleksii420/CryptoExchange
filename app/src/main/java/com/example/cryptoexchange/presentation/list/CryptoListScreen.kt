package com.example.cryptoexchange.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cryptoexchange.domain.model.Crypto
import com.example.cryptoexchange.presentation.components.CoinLogo
import com.example.cryptoexchange.presentation.components.DataAttribution
import com.example.cryptoexchange.presentation.components.MiniRangeBar
import com.example.cryptoexchange.presentation.format.formatPrice
import com.example.cryptoexchange.presentation.format.formatSyncTime
import com.example.cryptoexchange.presentation.format.formatTime
import com.example.cryptoexchange.presentation.format.rangePosition
import com.example.cryptoexchange.sync.SyncStatus
import com.example.cryptoexchange.ui.theme.CryptoExchangeTheme
import com.example.cryptoexchange.ui.theme.extraColors
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.Color

@Composable
fun CryptoListScreen(
    state: CryptoListUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    onCryptoClick: (String) -> Unit = {},
    selectedId: String? = null,
    listState: LazyListState = rememberLazyListState()
) {
    Column(modifier = modifier.fillMaxSize()) {
        ListHeader(
            status = state.syncStatus,
            lastSyncMillis = state.lastSyncMillis,
            hasData = state.cryptos.isNotEmpty(),
            onRefresh = onRefresh
        )
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.cryptos.isEmpty() -> EmptyState(
                onRefresh = onRefresh,
                refreshEnabled = state.syncStatus != SyncStatus.Running
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                items(items = state.cryptos, key = { it.id }) { crypto ->
                    CryptoRow(
                        crypto = crypto,
                        selected = crypto.id == selectedId,
                        onClick = { onCryptoClick(crypto.id) }
                    )
                }
                item { DataAttribution() }
            }
        }
    }
}

// ---------- Шапка: назва, кнопка ⟳, статус ----------

@Composable
private fun ListHeader(
    status: SyncStatus,
    lastSyncMillis: Long?,
    hasData: Boolean,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Крипто-біржа", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Топ-30 за капіталізацією · USD",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedIconButton(
                onClick = onRefresh,
                enabled = status != SyncStatus.Running,   // поки оновлюємось, кнопка неактивна
                modifier = Modifier.size(44.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Оновити")
            }
        }
        SyncStatusBanner(status, lastSyncMillis, hasData)
    }
}

// Перекладає SyncStatus на зрозумілий людині текст
@Composable
private fun SyncStatusBanner(status: SyncStatus, lastSyncMillis: Long?, hasData: Boolean) {
    val savedAt = lastSyncMillis?.let { formatSyncTime(it) }
    val savedNote = if (hasData && savedAt != null) " Показано дані станом на $savedAt." else ""

    when (status) {
        SyncStatus.Running ->
            StatusPill(PillKind.Neutral, "Оновлюємо дані…", showSpinner = true)
        SyncStatus.WaitingForNetwork ->
            StatusPill(
                PillKind.Warning,
                if (hasData) "Немає інтернету.$savedNote Оновимо, щойно з'явиться мережа."
                else "Очікуємо на мережу…"
            )
        SyncStatus.Retrying ->
            StatusPill(PillKind.Warning, "Не вдалося оновити, пробуємо ще раз…$savedNote")
        is SyncStatus.Failed ->
            StatusPill(PillKind.Warning, "${status.message}.$savedNote")
        SyncStatus.Idle, is SyncStatus.Succeeded ->
            if (savedAt != null) StatusPill(PillKind.Success, "Оновлено: $savedAt")
    }
}

private enum class PillKind { Success, Warning, Neutral }

@Composable
private fun StatusPill(kind: PillKind, text: String, showSpinner: Boolean = false) {
    val extra = MaterialTheme.extraColors
    val (background, content, dot) = when (kind) {
        PillKind.Success -> Triple(extra.successContainer, extra.onSuccessContainer, extra.successDot)
        PillKind.Warning -> Triple(extra.warningContainer, extra.onWarningContainer, extra.warningDot)
        PillKind.Neutral -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurface,
            MaterialTheme.colorScheme.primary
        )
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showSpinner) {
            CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = dot)
        } else {
            Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
        }
        Text(text, color = content, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

// ---------- Один рядок списку ----------

@Composable
private fun CryptoRow(crypto: Crypto, selected: Boolean, onClick: () -> Unit) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
            .clickable(onClickLabel = "Відкрити деталі", onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
    ) {
        Text(
            text = crypto.rank?.toString() ?: "—",
            modifier = Modifier.width(20.dp),
            textAlign = TextAlign.End,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = muted
        )
        CoinLogo(imageUrl = crypto.imageUrl, symbol = crypto.symbol, size = 40.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                crypto.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${crypto.symbol} / USD · ${formatTime(crypto.lastUpdatedMillis) ?: "—"}",
                fontSize = 12.sp,
                color = muted
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            val price = formatPrice(crypto.priceUsd)
            if (price != null) {
                Text(price, fontFamily = FontFamily.Monospace, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                rangePosition(crypto.priceUsd, crypto.low24h, crypto.high24h)?.let { MiniRangeBar(position = it) }
            } else {
                Text("Немає даних", fontSize = 13.sp, color = muted)
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

// ---------- База порожня ----------

@Composable
private fun EmptyState(onRefresh: () -> Unit, refreshEnabled: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Немає даних", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(
            "Курси ще не завантажувались. Підключіться до інтернету, і список з'явиться сам.",
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRefresh,
            enabled = refreshEnabled,
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Оновити", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(32.dp))
        DataAttribution()
    }
}

// ---------- Превʼю в Android Studio (без запуску) ----------

private val previewState = CryptoListUiState(
    isLoading = false,
    cryptos = listOf(
        Crypto(id = "bitcoin", name = "Bitcoin", symbol = "BTC", imageUrl = null, priceUsd = 61556.63,
            rank = 1, high24h = 62870.40, low24h = 60102.15, lastUpdatedMillis = System.currentTimeMillis()),
        Crypto(id = "tether", name = "Tether", symbol = "USDT", imageUrl = null, priceUsd = 1.0002,
            rank = 3, high24h = 1.001, low24h = 0.999, lastUpdatedMillis = System.currentTimeMillis()),
        Crypto(id = "test", name = "Монета без ціни", symbol = "TST", imageUrl = null, priceUsd = null,
            rank = 8, high24h = null, low24h = null, lastUpdatedMillis = null)
    ),
    lastSyncMillis = System.currentTimeMillis(),
    syncStatus = SyncStatus.Idle
)

@Preview(name = "Темна тема")
@Composable
private fun CryptoListDarkPreview() {
    CryptoExchangeTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            CryptoListScreen(state = previewState, onRefresh = {})
        }
    }
}

@Preview(name = "Світла тема")
@Composable
private fun CryptoListLightPreview() {
    CryptoExchangeTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            CryptoListScreen(state = previewState, onRefresh = {})
        }
    }
}