package com.expenseai.manager.util

import android.content.Context
import com.expenseai.manager.data.local.database.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val backupDir = File(context.filesDir, "backups").also { it.mkdirs() }
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    fun createBackup(): File {
        val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        val backupFile = File(backupDir, "backup_${dateFormat.format(Date())}.db")

        FileInputStream(dbFile).use { input ->
            FileOutputStream(backupFile).use { output ->
                input.copyTo(output)
            }
        }
        return backupFile
    }

    fun restoreBackup(backupFile: File): Boolean {
        return try {
            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            FileInputStream(backupFile).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getAvailableBackups(): List<File> =
        backupDir.listFiles { f -> f.name.endsWith(".db") }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()

    fun deleteBackup(backupFile: File): Boolean = backupFile.delete()
}
