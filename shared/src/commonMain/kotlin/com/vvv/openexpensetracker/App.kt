package com.vvv.openexpensetracker

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.vvv.openexpensetracker.presentation.screens.add_expense.AddExpenseIntent
import com.vvv.openexpensetracker.presentation.screens.add_expense.AddExpenseScreen
import com.vvv.openexpensetracker.presentation.screens.add_expense.AddExpenseViewModel
import com.vvv.openexpensetracker.presentation.screens.scan_receipt.ScanReceiptScreen
import com.vvv.openexpensetracker.presentation.screens.scan_receipt.ScanReceiptViewModel
import com.vvv.openexpensetracker.presentation.screens.expenses.ExpenseListViewModel
import com.vvv.openexpensetracker.presentation.screens.home.HomeScreen
import com.vvv.openexpensetracker.presentation.screens.settings.SettingsViewModel
import com.vvv.openexpensetracker.presentation.screens.stats.StatsViewModel
import com.vvv.openexpensetracker.presentation.theme.AppTheme
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
object HomeDestination

@Serializable
data class AddExpenseDestination(val expenseId: String? = null)

@Serializable
object ScanReceiptDestination

@Composable
fun App() {
    AppTheme {
        Surface {
            val navController: NavHostController = rememberNavController()

            // Resolve ViewModels via Koin Compose extensions
            val listViewModel: ExpenseListViewModel = koinViewModel()
            val statsViewModel: StatsViewModel = koinViewModel()
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val addViewModel: AddExpenseViewModel = koinViewModel()
            val scanViewModel: ScanReceiptViewModel = koinViewModel()

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
                        },
                        onNavigateToScan = {
                            navController.navigate(ScanReceiptDestination)
                        }
                    )
                }
                composable<ScanReceiptDestination> {
                    ScanReceiptScreen(
                        viewModel = scanViewModel,
                        onReceiptDetected = { text ->
                            addViewModel.onIntent(AddExpenseIntent.ReceiptScanned(text))
                        },
                        navigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
