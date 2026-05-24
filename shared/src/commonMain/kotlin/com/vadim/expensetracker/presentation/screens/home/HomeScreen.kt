package com.vadim.expensetracker.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vadim.expensetracker.presentation.screens.expenses.ExpenseListScreen
import com.vadim.expensetracker.presentation.screens.expenses.ExpenseListViewModel
import com.vadim.expensetracker.presentation.screens.stats.StatsScreen
import com.vadim.expensetracker.presentation.screens.stats.StatsViewModel
import com.vadim.expensetracker.presentation.screens.settings.SettingsScreen
import com.vadim.expensetracker.presentation.screens.settings.SettingsViewModel

@Composable
fun HomeScreen(
    listViewModel: ExpenseListViewModel,
    statsViewModel: StatsViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToAddEdit: (String?) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Expenses") },
                    label = { Text("Expenses", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Analytics") },
                    label = { Text("Analytics", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { onNavigateToAddEdit(null) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ExpenseListScreen(
                    viewModel = listViewModel,
                    onNavigateToAddEdit = onNavigateToAddEdit
                )
                1 -> StatsScreen(
                    viewModel = statsViewModel
                )
                2 -> SettingsScreen(
                    viewModel = settingsViewModel
                )
            }
        }
    }
}
