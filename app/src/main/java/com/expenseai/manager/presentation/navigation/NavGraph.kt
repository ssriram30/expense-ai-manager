package com.expenseai.manager.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.expenseai.manager.presentation.analytics.AnalyticsScreen
import com.expenseai.manager.presentation.auth.AuthScreen
import com.expenseai.manager.presentation.budget.AddBudgetScreen
import com.expenseai.manager.presentation.budget.BudgetScreen
import com.expenseai.manager.presentation.dashboard.DashboardScreen
import com.expenseai.manager.presentation.expense.AddEditExpenseScreen
import com.expenseai.manager.presentation.expense.ExpenseDetailScreen
import com.expenseai.manager.presentation.expense.ExpenseListScreen
import com.expenseai.manager.presentation.income.IncomeScreen
import com.expenseai.manager.presentation.insights.InsightsScreen
import com.expenseai.manager.presentation.ocr.OCRScanScreen
import com.expenseai.manager.presentation.search.SearchScreen
import com.expenseai.manager.presentation.settings.SettingsScreen
import com.expenseai.manager.presentation.transfer.TransferScreen

@Composable
fun ExpenseNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Dashboard.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(tween(300)) { it / 4 } + fadeIn(tween(300)) },
        exitTransition = { slideOutHorizontally(tween(300)) { -it / 4 } + fadeOut(tween(300)) },
        popEnterTransition = { slideInHorizontally(tween(300)) { -it / 4 } + fadeIn(tween(300)) },
        popExitTransition = { slideOutHorizontally(tween(300)) { it / 4 } + fadeOut(tween(300)) }
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(onAuthSuccess = {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToExpenseList = { navController.navigate(Screen.ExpenseList.route) },
                onNavigateToAddExpense = { navController.navigate(Screen.AddExpense.createRoute()) },
                onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) },
                onNavigateToBudget = { navController.navigate(Screen.Budget.route) },
                onNavigateToInsights = { navController.navigate(Screen.Insights.route) },
                onNavigateToIncome = { navController.navigate(Screen.Income.route) },
                onNavigateToTransfers = { navController.navigate(Screen.Transfers.route) },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToScan = { navController.navigate(Screen.OCRScan.route) }
            )
        }

        composable(Screen.ExpenseList.route) {
            ExpenseListScreen(
                onBack = { navController.popBackStack() },
                onAddExpense = { navController.navigate(Screen.AddExpense.createRoute()) },
                onExpenseClick = { navController.navigate(Screen.ExpenseDetail.createRoute(it)) },
                onScanReceipt = { navController.navigate(Screen.OCRScan.route) }
            )
        }

        composable(
            route = Screen.AddExpense.route,
            arguments = listOf(navArgument("expenseId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getLong("expenseId") ?: -1L
            AddEditExpenseScreen(
                expenseId = expenseId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onScanReceipt = { navController.navigate(Screen.OCRScan.route) }
            )
        }

        composable(
            route = Screen.ExpenseDetail.route,
            arguments = listOf(navArgument("expenseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getLong("expenseId") ?: return@composable
            ExpenseDetailScreen(
                expenseId = expenseId,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Screen.AddExpense.createRoute(expenseId)) }
            )
        }

        composable(Screen.OCRScan.route) {
            OCRScanScreen(
                onBack = { navController.popBackStack() },
                onResultReady = { navController.navigate(Screen.AddExpense.createRoute()) }
            )
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Budget.route) {
            BudgetScreen(
                onBack = { navController.popBackStack() },
                onAddBudget = { navController.navigate(Screen.AddBudget.createRoute()) },
                onBudgetClick = { navController.navigate(Screen.AddBudget.createRoute(it)) }
            )
        }

        composable(
            route = Screen.AddBudget.route,
            arguments = listOf(navArgument("budgetId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val budgetId = backStackEntry.arguments?.getLong("budgetId") ?: -1L
            AddBudgetScreen(
                budgetId = budgetId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(Screen.Insights.route) {
            InsightsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Income.route) {
            IncomeScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Transfers.route) {
            TransferScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onExpenseClick = { navController.navigate(Screen.ExpenseDetail.createRoute(it)) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
