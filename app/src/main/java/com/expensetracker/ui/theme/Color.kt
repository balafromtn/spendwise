package com.expensetracker.ui.theme

import androidx.compose.ui.graphics.Color

val TealGreen = Color(0xFF2E6F5E)
val TealGreenLight = Color(0xFF8FD9BE)
val Terracotta = Color(0xFFB85C38)
val TerracottaLight = Color(0xFFE8A47E)
val SurfaceLight = Color(0xFFFAF9F6)
val SurfaceDark = Color(0xFF1B1C1A)
val ErrorDark = Color(0xFFC1443C)
val ErrorLight = Color(0xFFF2A29C)

val IncomeGreen = Color(0xFF2E7D32)
val ExpenseRed = Color(0xFFC62828)
val WarningYellow = Color(0xFFF9A825)
val OverBudgetRed = Color(0xFFE53935)
val SafeGreen = Color(0xFF43A047)
val NearLimitOrange = Color(0xFFFF9800)

val ChartColors = listOf(
    Color(0xFF2E6F5E),
    Color(0xFFB85C38),
    Color(0xFF1976D2),
    Color(0xFF7B1FA2),
    Color(0xFFD32F2F),
    Color(0xFF00796B),
    Color(0xFF5D4037),
    Color(0xFF455A64),
    Color(0xFFC2185B),
    Color(0xFF0097A7)
)

enum class ThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}
