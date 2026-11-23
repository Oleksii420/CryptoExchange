package com.example.cryptoexchange.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.cryptoexchange.presentation.format.formatDateTime
import com.example.cryptoexchange.presentation.format.formatPrice
import com.example.cryptoexchange.presentation.format.formatSyncTime
import com.example.cryptoexchange.presentation.format.rangePosition
import com.example.cryptoexchange.ui.theme.CryptoExchangeTheme
import com.example.cryptoexchange.ui.theme.extraColors

// onBack = null → кнопки "Назад" немає (альбомний режим, де список і так поруч)
@Composable
fun CryptoDetailScreen(
    crypto: Crypto,
    lastSyncMillis: Long?,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val extra = MaterialTheme.extraColors

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())   // в альбомі екран низький, тому прокрутка
    ) {
        // ---------- Верхній рядок зі стрілкою "Назад" ----------
        if (onBack != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 20.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
                Text(
                    crypto.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // ---------- Логотип, назва, плашки ----------
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CoinLogo(imageUrl = crypto.imageUrl, symbol = crypto.symbol, size = 80.dp)
            Text(crypto.name, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip("${crypto.symbol} / USD", monospace = true)
                crypto.rank?.let { InfoChip("№ $it за капіталізацією") }
            }
        }

        // ---------- Ціна ----------
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Поточна ціна", fontSize = 13.sp, color = muted)
            val price = formatPrice(crypto.priceUsd)
            if (price != null) {
                Text(price, fontFamily = FontFamily.Monospace, fontSize = 36.sp, fontWeight = FontWeight.SemiBold)
            } else {
                Text("Немає даних", fontSize = 20.sp, color = muted)
            }
        }

        // ---------- Діапазон за 24 години ----------
        DetailCard(Modifier.padding(start = 16.dp, end = 16.dp, top = 28.dp)) {
            Text("Діапазон за 24 години", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            rangePosition(crypto.priceUsd, crypto.low24h, crypto.high24h)?.let { WideRangeBar(it) }
            Row(Modifier.fillMaxWidth()) {
                PriceLimit(
                    label = "Мінімум", value = formatPrice(crypto.low24h), color = extra.priceLow,
                    icon = Icons.Default.KeyboardArrowDown, alignEnd = false, modifier = Modifier.weight(1f)
                )
                PriceLimit(
                    label = "Максимум", value = formatPrice(crypto.high24h), color = extra.priceHigh,
                    icon = Icons.Default.KeyboardArrowUp, alignEnd = true, modifier = Modifier.weight(1f)
                )
            }
        }

        // ---------- Інформація ----------
        DetailCard(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)) {
            InfoRow("Оновлено на біржі", formatDateTime(crypto.lastUpdatedMillis) ?: "—")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            InfoRow("Збережено на телефоні", lastSyncMillis?.let { formatSyncTime(it) } ?: "—")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            InfoRow("ID у CoinGecko", crypto.id)
        }

        DataAttribution(Modifier.padding(top = 8.dp))
    }
}

// ---------- Маленькі помічники ----------

@Composable
private fun DetailCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

@Composable
private fun InfoChip(text: String, monospace: Boolean = false) {
    val shape = RoundedCornerShape(50)
    Text(
        text = text,
        modifier = Modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        fontSize = 13.sp,
        fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
        fontWeight = if (monospace) FontWeight.SemiBold else FontWeight.Normal,
        color = if (monospace) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// Велика шкала: крапка між мінімумом (ліворуч) і максимумом (праворуч)
@Composable
private fun WideRangeBar(position: Float) {
    val dot = 18.dp
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth().height(dot),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.outline)
        )
        Box(
            Modifier
                .offset(x = (maxWidth - dot) * position)   // maxWidth: ширина, яку дав BoxWithConstraints
                .size(dot)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface)
                .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
        )
    }
}

// Стрілка ↓/↑ разом із кольором: розрізнить навіть людина, яка погано бачить кольори
@Composable
private fun PriceLimit(
    label: String,
    value: String?,
    color: Color,
    icon: ImageVector,
    alignEnd: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Text(value ?: "—", color = color, fontFamily = FontFamily.Monospace, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
    }
}

// ---------- Превʼю: звичайна монета і монета БЕЗ даних ----------

@Preview(name = "Деталі: темна тема")
@Composable
private fun CryptoDetailPreview() {
    CryptoExchangeTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            CryptoDetailScreen(
                crypto = Crypto(
                    id = "bitcoin", name = "Bitcoin", symbol = "BTC", imageUrl = null,
                    priceUsd = 61556.63, rank = 1, high24h = 62870.40, low24h = 60102.15,
                    lastUpdatedMillis = System.currentTimeMillis()
                ),
                lastSyncMillis = System.currentTimeMillis(),
                onBack = {}
            )
        }
    }
}

@Preview(name = "Деталі: порожні поля")
@Composable
private fun CryptoDetailEmptyPreview() {
    CryptoExchangeTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            CryptoDetailScreen(
                crypto = Crypto(
                    id = "test", name = "Монета без даних", symbol = "TST", imageUrl = null,
                    priceUsd = null, rank = null, high24h = null, low24h = null,
                    lastUpdatedMillis = null
                ),
                lastSyncMillis = null,
                onBack = null
            )
        }
    }
}