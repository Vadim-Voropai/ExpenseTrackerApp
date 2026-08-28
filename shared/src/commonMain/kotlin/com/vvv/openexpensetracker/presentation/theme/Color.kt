package com.vvv.openexpensetracker.presentation.theme

import androidx.compose.ui.graphics.Color
import com.vvv.openexpensetracker.domain.model.Category

// Category Colors
val ColorFood = Color(0xFFFF9800)
val ColorTransport = Color(0xFF2196F3)
val ColorCar = Color(0xFF607D8B)
val ColorSport = Color(0xFF4CAF50)
val ColorBills = Color(0xFFF44336)
val ColorEntertainment = Color(0xFF9C27B0)
val ColorCloses = Color(0xFFE91E63)
val ColorHealth = Color(0xFF00BCD4)
val ColorOthers = Color(0xFF757575)

// Functional Colors
val ColorSuccess = Color(0xFF4CAF50)
val ColorWarning = Color(0xFFFFC107)
val ColorError = Color(0xFFF44336)

// Brand Colors
val PrimaryColor = Color(0xFF6200EE)
val PrimaryVariantColor = Color(0xFF3700B3)
val SecondaryColor = Color(0xFF03DAC6)

// Neutral Colors
val BackgroundLight = Color(0xFFF8F9FA)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF1C1B1F)

val BackgroundDark = Color(0xFF121212)
val SurfaceDark = Color(0xFF1E1E1E)
val OnSurfaceDark = Color(0xFFE6E1E5)

fun getCategoryColor(category: String): Color {
    return when (category) {
        Category.FOOD -> ColorFood
        Category.TRANSPORT -> ColorTransport
        Category.CAR -> ColorCar
        Category.SPORT -> ColorSport
        Category.BILLS -> ColorBills
        Category.ENTERTAINMENT -> ColorEntertainment
        Category.CLOSES -> ColorCloses
        Category.HEALTH -> ColorHealth
        else -> ColorOthers
    }
}
