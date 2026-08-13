package com.vvv.openexpensetracker.presentation.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object AppIcons {
    val Food = Icons.Default.ShoppingCart
    val Transport = Icons.Default.LocationOn
    val Utilities = Icons.Default.Home
    val Entertainment = Icons.Default.PlayArrow
    val Health = Icons.Default.Favorite
    val Shopping = Icons.Default.AccountBox
    val Others = Icons.Default.Info
    
    val Sync = Icons.Default.Refresh
    val Search = Icons.Default.Search
    val Clear = Icons.Default.Close
    val Delete = Icons.Default.Delete
    val ArrowBack = Icons.Default.ArrowBack
    val Date = Icons.Default.DateRange
    val Done = Icons.Default.Done
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Food" -> AppIcons.Food
        "Transport" -> AppIcons.Transport
        "Utilities" -> AppIcons.Utilities
        "Entertainment" -> AppIcons.Entertainment
        "Health" -> AppIcons.Health
        "Shopping" -> AppIcons.Shopping
        else -> AppIcons.Others
    }
}
