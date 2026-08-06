package com.expensetracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val SpendwiseLightScheme = lightColorScheme(
    primary = BrandPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEDE6),
    onPrimaryContainer = Color(0xFF0F4C42),
    secondary = BrandSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBEAFE),
    onSecondaryContainer = Color(0xFF1E3A8A),
    tertiary = Color(0xFF0EA5E9),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE0F2FE),
    onTertiaryContainer = Color(0xFF082F49),
    background = BackgroundLight,
    onBackground = TextLight,
    surface = SurfaceLight,
    onSurface = TextLight,
    surfaceVariant = Color(0xFFECE5E6),
    onSurfaceVariant = Color(0xFF6B5F66),
    surfaceContainerLowest = CardLight,
    surfaceContainerLow = Color(0xFFF4F2F3),
    surfaceContainer = Color(0xFFEBE9EA),
    surfaceContainerHigh = Color(0xFFE3E0E2),
    surfaceContainerHighest = Color(0xFFDCD8DA),
    outline = Color(0xFF8A7F87),
    outlineVariant = Color(0xFFD2C9CD),
    error = ExpenseRed,
    onError = Color.White,
    errorContainer = Color(0xFFF6D3CB),
    onErrorContainer = Color(0xFF4A0D04),
    inverseSurface = TextLight,
    inverseOnSurface = TextDark,
    inversePrimary = BrandPrimaryDark,
    surfaceTint = BrandPrimaryLight
)

private val SpendwiseDarkScheme = darkColorScheme(
    primary = BrandPrimaryDark,
    onPrimary = Color(0xFF0A332D),
    primaryContainer = Color(0xFF2E6A60),
    onPrimaryContainer = Color(0xFFCDEDE6),
    secondary = BrandSecondaryDark,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF1E3A8A),
    onSecondaryContainer = Color(0xFFDBEAFE),
    tertiary = Color(0xFF38BDF8),
    onTertiary = Color(0xFF082F49),
    tertiaryContainer = Color(0xFF0284C7),
    onTertiaryContainer = Color(0xFFE0F2FE),
    background = BackgroundDark,
    onBackground = TextDark,
    surface = SurfaceDark,
    onSurface = TextDark,
    surfaceVariant = Color(0xFF5F585F),
    onSurfaceVariant = Color(0xFFC4BBC0),
    surfaceContainerLowest = Color(0xFF464046),
    surfaceContainerLow = SurfaceDark,
    surfaceContainer = CardDark,
    surfaceContainerHigh = Color(0xFF6B646B),
    surfaceContainerHighest = Color(0xFF766F76),
    outline = Color(0xFF9C9198),
    outlineVariant = Color(0xFF5F585F),
    error = Color(0xFFF08B72),
    onError = Color(0xFF3A0D04),
    errorContainer = Color(0xFF6E2A18),
    onErrorContainer = Color(0xFFF9D9CC),
    inverseSurface = TextDark,
    inverseOnSurface = TextLight,
    inversePrimary = BrandPrimaryLight,
    surfaceTint = BrandPrimaryDark
)

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val effectiveDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> darkTheme
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (effectiveDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        effectiveDarkTheme -> SpendwiseDarkScheme
        else -> SpendwiseLightScheme
    }

    CompositionLocalProvider(
        LocalBrandGradient provides (if (effectiveDarkTheme) BrandGradientDark else BrandGradientLight)
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
