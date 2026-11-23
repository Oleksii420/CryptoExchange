package com.example.cryptoexchange.presentation.format

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val usSymbols = DecimalFormatSymbols(Locale.US)

fun formatPrice(price: Double?): String? {
    if (price == null) return null
    val text = when {
        price >= 1_000 -> DecimalFormat("#,##0.00", usSymbols).format(price)
        price >= 1 -> DecimalFormat("#,##0.00##", usSymbols).format(price)
        price == 0.0 -> "0"
        else -> BigDecimal.valueOf(price)
            .round(MathContext(6, RoundingMode.HALF_UP))
            .stripTrailingZeros()
            .toPlainString()
    }
    return "\$$text"
}

fun formatTime(millis: Long?): String? =
    millis?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it)) }

fun formatDateTime(millis: Long?): String? =
    millis?.let { SimpleDateFormat("dd.MM.yyyy, HH:mm:ss", Locale.getDefault()).format(Date(it)) }

fun formatSyncTime(millis: Long): String {
    val then = Calendar.getInstance().apply { timeInMillis = millis }
    val now = Calendar.getInstance()
    val sameDay = then.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            then.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
    val pattern = if (sameDay) "HH:mm" else "dd.MM, HH:mm"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
}

fun rangePosition(price: Double?, low: Double?, high: Double?): Float? {
    if (price == null || low == null || high == null || high <= low) return null
    return ((price - low) / (high - low)).toFloat().coerceIn(0f, 1f)
}