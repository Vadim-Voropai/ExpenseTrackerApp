package com.vvv.openexpensetracker.presentation.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppDimens(
    val spacingExtraSmall: Dp = 4.dp,
    val spacingSmall: Dp = 8.dp,
    val spacingNormal: Dp = 16.dp,
    val spacingLarge: Dp = 24.dp,
    val spacingExtraLarge: Dp = 32.dp,
    
    val cornerRadiusSmall: Dp = 8.dp,
    val cornerRadiusNormal: Dp = 12.dp,
    val cornerRadiusLarge: Dp = 16.dp,
    val cornerRadiusExtraLarge: Dp = 20.dp,
    
    val iconSizeSmall: Dp = 16.dp,
    val iconSizeNormal: Dp = 24.dp,
    val iconSizeLarge: Dp = 36.dp,
    val iconSizeExtraLarge: Dp = 48.dp,
    val categoryIconSize: Dp = 80.dp,
    
    val borderWidthNormal: Dp = 1.dp,
    val borderWidthSelected: Dp = 2.dp,
    
    val strokeWidthSmall: Dp = 2.dp,
    
    val buttonHeight: Dp = 56.dp,
    val cardPadding: Dp = 16.dp,
    val chartSize: Dp = 240.dp
)

val LocalAppDimens = staticCompositionLocalOf { AppDimens() }
