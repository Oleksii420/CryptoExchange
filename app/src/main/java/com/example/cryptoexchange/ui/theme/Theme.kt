package com.example.cryptoexchange.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Mint,
    onPrimary = MintOn,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkOutline,
    outlineVariant = DarkDivider
)

private val LightColors = lightColorScheme(
    primary = MintDark,
    onPrimary = Color.White,
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextMuted,
    outline = LightOutline,
    outlineVariant = LightDivider
)

data class ExtraColors(
    val successContainer: Color,
    val onSuccessContainer: Color,
    val successDot: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val warningDot: Color,
    val priceLow: Color,
    val priceHigh: Color
)

private val DarkExtraColors = ExtraColors(
    successContainer = Color(0xFF10251E), onSuccessContainer = Color(0xFF6EE7B7), successDot = Mint,
    warningContainer = Color(0xFF2A1E10), onWarningContainer = Color(0xFFFFB27A), warningDot = Color(0xFFFF9A52),
    priceLow = Color(0xFFFF9A52), priceHigh = Color(0xFF60B6FF)
)

private val LightExtraColors = ExtraColors(
    successContainer = Color(0xFFDDF5EA), onSuccessContainer = Color(0xFF065F46), successDot = Color(0xFF059669),
    warningContainer = Color(0xFFFFF1E0), onWarningContainer = Color(0xFF7C3D0A), warningDot = Color(0xFFC2570C),
    priceLow = Color(0xFFB45309), priceHigh = Color(0xFF1D64C9)
)

private val LocalExtraColors = staticCompositionLocalOf { DarkExtraColors }

val MaterialTheme.extraColors: ExtraColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtraColors.current

@Composable
fun CryptoExchangeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalExtraColors provides if (darkTheme) DarkExtraColors else LightExtraColors
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = Typography,
            content = content
        )
    }
}