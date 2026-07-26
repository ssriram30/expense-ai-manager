package com.expenseai.manager.presentation.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    object ExpenseList : Screen("expense_list")
    object AddExpense : Screen("add_expense?expenseId={expenseId}") {
        fun createRoute(expenseId: Long = -1) = "add_expense?expenseId=$expenseId"
    }
    object ExpenseDetail : Screen("expense_detail/{expenseId}") {
        fun createRoute(expenseId: Long) = "expense_detail/$expenseId"
    }
    object OCRScan : Screen("ocr_scan")
    object Analytics : Screen("analytics")
    object Budget : Screen("budget")
    object AddBudget : Screen("add_budget?budgetId={budgetId}") {
        fun createRoute(budgetId: Long = -1) = "add_budget?budgetId=$budgetId"
    }
    object Insights : Screen("insights")
    object Income : Screen("income")
    object Transfers : Screen("transfers")
    object Search : Screen("search")
    object Settings : Screen("settings")
    object Export : Screen("export")
    object RecurringExpenses : Screen("recurring_expenses")
}
