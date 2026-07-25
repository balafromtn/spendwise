package com.expensetracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val SpendwiseLightScheme = lightColorScheme(
    primary = TealGreen,
    onPrimary = Color.White,
    primaryContainer = TealGreenLight,
    onPrimaryContainer = Color(0xFF002114),
    secondary = Terracotta,
    onSecondary = Color.White,
    secondaryContainer = TerracottaLight,
    onSecondaryContainer = Color(0xFF3B1A00),
    tertiary = Color(0xFF4A6267),
    background = SurfaceLight,
    onBackground = Color(0xFF1B1C1A),
    surface = SurfaceLight,
    onSurface = Color(0xFF1B1C1A),
    surfaceVariant = Color(0xFFE0E3DF),
    onSurfaceVariant = Color(0xFF434843),
    error = ErrorDark,
    onError = Color.White
)

private val SpendwiseDarkScheme = darkColorScheme(
    primary = TealGreenLight,
    onPrimary = Color(0xFF003827),
    primaryContainer = TealGreen,
    onPrimaryContainer = TealGreenLight,
    secondary = TerracottaLight,
    onSecondary = Color(0xFF5A2A10),
    secondaryContainer = Terracotta,
    onSecondaryContainer = TerracottaLight,
    tertiary = Color(0xFFB1CACF),
    background = SurfaceDark,
    onBackground = Color(0xFFE1E3E0),
    surface = SurfaceDark,
    onSurface = Color(0xFFE1E3E0),
    surfaceVariant = Color(0xFF434843),
    onSurfaceVariant = Color(0xFFC3C8C2),
    error = ErrorLight,
    onError = Color(0xFF601410)
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

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
