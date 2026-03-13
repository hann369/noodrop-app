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
    primaryContainer     = NdOrange.copy(alpha = 0.12f),
    onPrimaryContainer   = NdOrange,
    secondary            = Color(0xFF9A9A9A),
    onSecondary          = Color(0xFF111111),
    secondaryContainer   = Color(0xFF1A1A1C),
    onSecondaryContainer = Color(0xFF9A9A9A),
    background           = Color(0xFF0F0F11),   // slightly lifted from pure black — less harsh
    onBackground         = Color(0xFFEEEEF0),
    surface              = Color(0xFF191919),   // cards breathe more against bg
    onSurface            = Color(0xFFEEEEF0),
    surfaceVariant       = Color(0xFF242426),   // inner chips/inputs
    onSurfaceVariant     = Color(0xFF7A7A80),
    outline              = Color(0xFF333336),
    outlineVariant       = Color(0xFF252527),
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
    displayLarge  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold,      fontSize = 48.sp, lineHeight = 54.sp,  letterSpacing = (-1).sp),
    displayMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold,      fontSize = 36.sp, lineHeight = 42.sp,  letterSpacing = (-0.5).sp),
    displaySmall  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold,      fontSize = 28.sp, lineHeight = 34.sp,  letterSpacing = (-0.25).sp),
    headlineLarge  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp,  letterSpacing = (-0.2).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp,  letterSpacing = (-0.15).sp),
    headlineSmall  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleLarge  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold,    fontSize = 16.sp, lineHeight = 22.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,      fontSize = 15.sp, lineHeight = 21.sp),
    titleSmall  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,      fontSize = 13.sp, lineHeight = 18.sp),
    bodyLarge   = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,      fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,      fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall   = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,      fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold,    fontSize = 13.sp, lineHeight = 18.sp,  letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,      fontSize = 12.sp, lineHeight = 16.sp,  letterSpacing = 0.2.sp),
    labelSmall  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,      fontSize = 10.sp, lineHeight = 14.sp,  letterSpacing = 0.5.sp),
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
