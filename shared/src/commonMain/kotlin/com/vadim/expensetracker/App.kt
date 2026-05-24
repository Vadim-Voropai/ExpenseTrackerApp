package com.vadim.expensetracker

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.vadim.expensetracker.presentation.screens.add_expense.AddExpenseScreen
import com.vadim.expensetracker.presentation.screens.add_expense.AddExpenseViewModel
import com.vadim.expensetracker.presentation.screens.expenses.ExpenseListViewModel
import com.vadim.expensetracker.presentation.screens.home.HomeScreen
import com.vadim.expensetracker.presentation.screens.settings.SettingsViewModel
import com.vadim.expensetracker.presentation.screens.stats.StatsViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
object HomeDestination

@Serializable
data class AddExpenseDestination(val expenseId: String? = null)

@Composable
fun App() {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    ) {
        Surface {
            val navController: NavHostController = rememberNavController()

            // Resolve ViewModels via Koin Compose extensions
            val listViewModel: ExpenseListViewModel = koinViewModel()
            val statsViewModel: StatsViewModel = koinViewModel()
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val addViewModel: AddExpenseViewModel = koinViewModel()

            NavHost(navController = navController, startDestination = HomeDestination) {
                composable<HomeDestination> {
                    HomeScreen(
                        listViewModel = listViewModel,
                        statsViewModel = statsViewModel,
                        settingsViewModel = settingsViewModel,
                        onNavigateToAddEdit = { id ->
                            navController.navigate(AddExpenseDestination(id))
                        }
                    )
                }
                composable<AddExpenseDestination> { backStackEntry ->
                    val destination = backStackEntry.toRoute<AddExpenseDestination>()
                    AddExpenseScreen(
                        viewModel = addViewModel,
                        expenseId = destination.expenseId,
                        navigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
