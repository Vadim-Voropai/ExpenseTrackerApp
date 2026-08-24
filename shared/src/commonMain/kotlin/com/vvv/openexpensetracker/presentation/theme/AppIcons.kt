package com.vvv.openexpensetracker.presentation.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
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
    val Scan = Icons.Default.Add
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
