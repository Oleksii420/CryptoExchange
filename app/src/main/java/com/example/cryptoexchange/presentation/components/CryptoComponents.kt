package com.example.cryptoexchange.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

private val LogoTints = listOf(
    Color(0xFFF5B04B), Color(0xFF9FA8F5), Color(0xFF5ED3A8), Color(0xFFB8C2CF),
    Color(0xFFF2D16B), Color(0xFFC49CF2), Color(0xFF7FB8F5), Color(0xFFE3C08A)
)

@Composable
fun CoinLogo(imageUrl: String?, symbol: String, size: Dp, modifier: Modifier = Modifier) {
    var loaded by remember(imageUrl) { mutableStateOf(false) }
    val tint = LogoTints[symbol.hashCode().mod(LogoTints.size)]

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (loaded) Color.Transparent else tint),
        contentAlignment = Alignment.Center
    ) {
        if (!loaded) {
            Text(
                text = symbol.take(1),
                color = Color(0xFF0B0F14),
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.4f).sp
            )
        }
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                onSuccess = { loaded = true }
            )
        }
    }
}

@Composable
fun MiniRangeBar(position: Float, modifier: Modifier = Modifier, width: Dp = 64.dp) {
    val dot = 10.dp
    Box(modifier = modifier.width(width).height(dot), contentAlignment = Alignment.CenterStart) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.outline)
        )
        Box(
            Modifier
                .offset(x = (width - dot) * position)
                .size(dot)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface)
        )
    }
}

@Composable
fun DataAttribution(modifier: Modifier = Modifier) {
    Text(
        text = "Дані: CoinGecko",
        modifier = modifier.fillMaxWidth().padding(vertical = 16.dp),
        textAlign = TextAlign.Center,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}