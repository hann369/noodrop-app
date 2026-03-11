package com.noodrop.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Brand colours ──────────────────────────────────────────────────────────
val NdOrange = Color(0xFFFF6B35)
val NdGreen  = Color(0xFF4CAF50)
val NdBlue   = Color(0xFF2196F3)
val NdPurple = Color(0xFF9C27B0)
val NdRed    = Color(0xFFF44336)

// ── Dark palette ────────────────────────────────────────────────────────────
private val DarkColors = darkColorScheme(
    primary              = NdOrange,
    onPrimary            = Color.White,
    primaryContainer     = NdOrange.copy(alpha = 0.15f),
    onPrimaryContainer   = NdOrange,
    secondary            = Color(0xFFAAAAAA),
    onSecondary          = Color(0xFF111111),
    secondaryContainer   = Color(0xFF1C1C1E),
    onSecondaryContainer = Color(0xFFAAAAAA),
    background           = Color(0xFF111111),   // Dropset true black
    onBackground         = Color(0xFFF2F2F2),
    surface              = Color(0xFF1C1C1E),   // Dropset card surface
    onSurface            = Color(0xFFF2F2F2),
    surfaceVariant       = Color(0xFF2C2C2E),   // Dropset inner surface
    onSurfaceVariant     = Color(0xFF8A8A8E),
    outline              = Color(0xFF3A3A3C),
    outlineVariant       = Color(0xFF2C2C2E),
    error                = Color(0xFFFF453A),
    onError              = Color.White,
    errorContainer       = Color(0xFF7C1A1A),
    onErrorContainer     = Color(0xFFFFDAD6),
)

// ── Light palette ───────────────────────────────────────────────────────────
private val LightColors = lightColorScheme(
    primary              = NdOrange,
    onPrimary            = Color.White,
    primaryContainer     = NdOrange.copy(alpha = 0.12f),
    onPrimaryContainer   = Color(0xFF8B2500),
    secondary            = Color(0xFF455A64),
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFECEFF1),
    onSecondaryContainer = Color(0xFF263238),
    background           = Color(0xFFF5F5F7),
    onBackground         = Color(0xFF1A1A2E),
    surface              = Color.White,
    onSurface            = Color(0xFF1A1A2E),
    surfaceVariant       = Color(0xFFECEFF1),
    onSurfaceVariant     = Color(0xFF607D8B),
    outline              = Color(0xFFCFD8DC),
    outlineVariant       = Color(0xFFE0E0E0),
    error                = Color(0xFFB00020),
    onError              = Color.White,
    errorContainer       = Color(0xFFFFDAD6),
    onErrorContainer     = Color(0xFF410002),
)

// ── Typography ───────────────────────────────────────────────────────────────
// Uses system default font family. If you have Clash Display fonts, replace
// FontFamily.Default with your custom FontFamily here.
private val AppTypography = Typography(
    displayLarge  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold,   fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold,   fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold,   fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,   fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge   = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall   = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,   fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,   fontSize = 11.sp, lineHeight = 16.sp),
)

// ── Theme composable ─────────────────────────────────────────────────────────
@Composable
fun NoodropTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography  = AppTypography,
        content     = content,
    )
}
