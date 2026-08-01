package com.expensetracker.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import java.lang.Math

// Brand palette (Fintech Blue)
val BrandPrimaryLight = Color(0xFF1D4ED8)   // Deep blue
val BrandSecondaryLight = Color(0xFF3B82F6) // Primary blue
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val CardLight = Color(0xFFFFFFFF)
val TextLight = Color(0xFF1E293B)           // Dark slate

val BrandPrimaryDark = Color(0xFF60A5FA)    // Light blue
val BrandSecondaryDark = Color(0xFF93C5FD)  // Pale blue
val BackgroundDark = Color(0xFF0F172A)      // Very dark slate
val SurfaceDark = Color(0xFF1E293B)         // Dark slate
val CardDark = Color(0xFF334155)            // Medium slate
val TextDark = Color(0xFFF1F5F9)

val TealDeep = Color(0xFF1E3A8A) // Replaced with deep blue
val TealLight = Color(0xFF60A5FA) // Replaced with light blue
val PeachSoft = Color(0xFF93C5FD) // Replaced with pale blue

// Semantic colors
val IncomeGreen = Color(0xFF4FAB9A)
val ExpenseRed = Color(0xFFA5270F)
val WarningYellow = Color(0xFFC03C15)
val OverBudgetRed = Color(0xFFA5270F)
val SafeGreen = Color(0xFF4FAB9A)
val NearLimitOrange = Color(0xFFC03C15)

val BlueDeep = Color(0xFF1E3A8A)
val BluePrimary = Color(0xFF3B82F6)
val BlueLight = Color(0xFF93C5FD)

// Income/expense variants that stay readable on the brand gradient (blue)
val IncomeOnBrand = Color(0xFF4ADE80) // Green
val ExpenseOnBrand = Color(0xFFF87171) // Red

val ChartColors = listOf(
    Color(0xFF1E3A8A),
    Color(0xFF1E40AF),
    Color(0xFF1D4ED8),
    Color(0xFF2563EB),
    Color(0xFF3B82F6),
    Color(0xFF60A5FA),
    Color(0xFF93C5FD),
    Color(0xFFBFDBFE),
    Color(0xFF6366F1), // Indigo shade
    Color(0xFF818CF8)  // Light indigo
)

// Deterministic unique color per category
fun categoryColor(category: String): Color =
    ChartColors[Math.floorMod(category.hashCode(), ChartColors.size)]

// Brand gradients: blue shades
val BrandGradientLight = Brush.linearGradient(colors = listOf(BluePrimary, BlueLight))
val BrandGradientDark = Brush.linearGradient(colors = listOf(BlueDeep, BluePrimary))

val LocalBrandGradient = compositionLocalOf { BrandGradientLight }

enum class ThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}
