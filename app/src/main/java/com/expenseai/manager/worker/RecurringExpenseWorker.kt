package com.expenseai.manager.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.expenseai.manager.domain.model.Expense
import com.expenseai.manager.domain.model.TransactionType
import com.expenseai.manager.domain.repository.ExpenseRepository
import com.expenseai.manager.domain.repository.RecurringExpenseRepository
import com.expenseai.manager.util.CurrencyUtils
import com.expenseai.manager.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

@HiltWorker
class RecurringExpenseWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val recurringRepo: RecurringExpenseRepository,
    private val expenseRepo: ExpenseRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val due = recurringRepo.getDueRecurringExpenses(Date()).first()
            due.forEach { recurring ->
                val expense = Expense(
                    title = recurring.title,
                    amount = recurring.amount,
                    currency = recurring.currency,
                    category = recurring.category,
                    date = Date(),
                    paymentMethod = recurring.paymentMethod,
                    notes = "Auto-created from recurring: ${recurring.notes}",
                    isRecurring = true,
                    type = TransactionType.EXPENSE
                )
                expenseRepo.insertExpense(expense)

                val nextDate = Calendar.getInstance().apply {
                    time = recurring.nextDueDate
                    add(Calendar.DAY_OF_YEAR, recurring.frequency.daysInterval)
                }.time

                recurringRepo.updateRecurringExpense(recurring.copy(nextDueDate = nextDate))

                if (recurring.reminderEnabled) {
                    notificationHelper.showRecurringExpenseReminder(
                        recurring.title,
                        CurrencyUtils.format(recurring.amount, recurring.currency)
                    )
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "RecurringExpenseWorker"

        fun schedule(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<RecurringExpenseWorker>(1, TimeUnit.DAYS)
                .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
