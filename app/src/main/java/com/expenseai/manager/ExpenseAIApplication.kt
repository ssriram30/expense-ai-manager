package com.expenseai.manager

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ExpenseAIApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            listOf(
                NotificationChannel(
                    CHANNEL_BUDGET_ALERTS,
                    getString(R.string.channel_budget_alerts),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Alerts when budget thresholds are exceeded" },

                NotificationChannel(
                    CHANNEL_RECURRING,
                    getString(R.string.channel_recurring),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Reminders for recurring expenses" },

                NotificationChannel(
                    CHANNEL_BACKUP,
                    getString(R.string.channel_backup),
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = "Backup and sync notifications" }
            ).forEach { manager.createNotificationChannel(it) }
        }
    }

    companion object {
        const val CHANNEL_BUDGET_ALERTS = "budget_alerts"
        const val CHANNEL_RECURRING = "recurring_expenses"
        const val CHANNEL_BACKUP = "backup_reminders"
    }
}
