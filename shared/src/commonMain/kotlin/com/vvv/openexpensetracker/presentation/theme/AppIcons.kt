package com.vvv.openexpensetracker.presentation.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.vvv.openexpensetracker.domain.model.Category

object AppIcons {
    val Food = Icons.Default.ShoppingCart
    val Transport = Icons.Default.LocationOn
    val Car = Icons.Default.Build
    val Sport = Icons.Default.Star
    val Bills = Icons.Default.Home
    val Entertainment = Icons.Default.PlayArrow
    val Closes = Icons.Default.AccountBox
    val Health = Icons.Default.Favorite
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
        Category.FOOD -> AppIcons.Food
        Category.TRANSPORT -> AppIcons.Transport
        Category.CAR -> AppIcons.Car
        Category.SPORT -> AppIcons.Sport
        Category.BILLS -> AppIcons.Bills
        Category.ENTERTAINMENT -> AppIcons.Entertainment
        Category.CLOSES -> AppIcons.Closes
        Category.HEALTH -> AppIcons.Health
        else -> AppIcons.Others
    }
}
