package com.noodrop.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// - Colors -
val NdOrange       = Color(0xFFF3701E)
val NdOrangeLight  = Color(0xFFF5874A)
val NdCream        = Color(0xFFE8D8C9)
val NdCreamDark    = Color(0xFF1E2A3A)
val NdSlate        = Color(0xFF4B607F)
val NdSlateDark    = Color(0xFF8FABD4)
val NdDark         = Color(0xFF2C3E50)
val NdDarkBg       = Color(0xFF0A0E17)
val NdDarkCard     = Color(0xFF111827)
val NdDarkBorder   = Color(0xFF1E2A3A)
val NdBg           = Color(0xFFFAFAF8)
val NdGreen        = Color(0xFF22C55E)
val NdRed          = Color(0xFFEF4444)
val NdBlue         = Color(0xFF3B82F6)
val NdPurple       = Color(0xFFA855F7)
val NdMuted        = Color(0xFF8A9AB0)
val NdMutedDark    = Color(0xFF5A6A80)

private val LightScheme = lightColorScheme(
    primary            = NdOrange,
    onPrimary          = Color.White,
    primaryContainer   = NdCream,
    onPrimaryContainer = NdDark,
    secondary          = NdSlate,
    onSecondary        = Color.White,
    background         = NdBg,
    onBackground       = NdDark,
    surface            = Color.White,
    onSurface          = NdDark,
    surfaceVariant     = NdCream,
    onSurfaceVariant   = NdSlate,
    outline            = NdCream,
    outlineVariant     = NdCream,
    error              = NdRed,
)

private val DarkScheme = darkColorScheme(
    primary            = NdOrangeLight,
    onPrimary          = Color.White,
    primaryContainer   = NdCreamDark,
    onPrimaryContainer = Color(0xFFE0E0E0),
    secondary          = NdSlateDark,
    onSecondary        = NdDarkBg,
    background         = NdDarkBg,
    onBackground       = Color(0xFFE0E0E0),
    surface            = NdDarkCard,
    onSurface          = Color(0xFFE0E0E0),
    surfaceVariant     = NdDarkBorder,
    onSurfaceVariant   = NdSlateDark,
    outline            = NdDarkBorder,
    outlineVariant     = NdDarkBorder,
    error              = NdRed,
)

// - Typography -
// Font files must live in res/font/:
//   clashdisplay_regular.ttf, clashdisplay_medium.ttf,
//   clashdisplay_semibold.ttf, clashdisplay_bold.ttf
val ClashDisplay = FontFamily.Default

val NoodropTypography = Typography(
    displayLarge  = TextStyle(fontFamily = ClashDisplay, fontWeight = FontWeight.Bold,     fontSize = 40.sp, letterSpacing = (-1.5).sp),
    displayMedium = TextStyle(fontFamily = ClashDisplay, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, letterSpacing = (-1).sp),
    displaySmall  = TextStyle(fontFamily = ClashDisplay, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontFamily = ClashDisplay, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    headlineMedium= TextStyle(fontFamily = ClashDisplay, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    headlineSmall = TextStyle(fontFamily = ClashDisplay, fontWeight = FontWeight.Medium,   fontSize = 15.sp),
    titleLarge    = TextStyle(fontFamily = ClashDisplay, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 0.1.sp),
    titleSmall    = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 11.sp, letterSpacing = 0.5.sp),
    bodyLarge     = TextStyle(fontSize = 16.sp),
    bodyMedium    = TextStyle(fontSize = 14.sp),
    bodySmall     = TextStyle(fontSize = 12.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp),
    labelMedium   = TextStyle(fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.5.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Bold, fontSize = 9.sp,  letterSpacing = 1.sp),
)

// - Composition local for accent color -
val LocalNdOrange = staticCompositionLocalOf { NdOrange }

// - Theme -
@Composable
fun NoodropTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val w = (view.context as Activity).window
            w.statusBarColor = scheme.background.toArgb()
            WindowCompat.getInsetsController(w, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    CompositionLocalProvider(LocalNdOrange provides NdOrange) {
        MaterialTheme(colorScheme = scheme, typography = NoodropTypography, content = content)
    }
}
