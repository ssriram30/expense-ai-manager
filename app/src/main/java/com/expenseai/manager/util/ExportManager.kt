package com.expenseai.manager.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.expenseai.manager.domain.model.Expense
import com.expenseai.manager.domain.model.Income
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private fun getExportDir(): File {
        val dir = File(context.filesDir, "exports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun escapeCsv(value: String): String {
        val needsQuote = value.contains(',') || value.contains('"') || value.contains('\n')
        return if (needsQuote) "\"${value.replace("\"", "\"\"")}\"" else value
    }

    private fun csvRow(vararg values: String): String =
        values.joinToString(",") { escapeCsv(it) }

    fun exportExpensesCSV(expenses: List<Expense>, filename: String = "expenses_${Date().time}.csv"): File {
        val file = File(getExportDir(), filename)
        try {
            FileWriter(file).use { writer ->
                writer.appendLine(csvRow("ID", "Title", "Description", "Amount", "Currency",
                    "Category", "Merchant", "Date", "Payment Method", "Notes", "Tags", "Tax", "Type"))
                expenses.forEach { e ->
                    writer.appendLine(csvRow(
                        e.id.toString(), e.title, e.description, e.amount.toString(),
                        e.currency, e.category.displayName, e.merchant,
                        DateUtils.formatISO(e.date), e.paymentMethod.displayName,
                        e.notes, e.tags.joinToString(";"), e.taxAmount.toString(), e.type.name
                    ))
                }
            }
        } catch (e: IOException) {
            throw RuntimeException("Failed to export CSV: ${e.message}", e)
        }
        return file
    }

    fun exportIncomeCSV(incomes: List<Income>, filename: String = "income_${Date().time}.csv"): File {
        val file = File(getExportDir(), filename)
        try {
            FileWriter(file).use { writer ->
                writer.appendLine(csvRow("ID", "Title", "Amount", "Currency", "Source", "Date", "Notes", "Tags"))
                incomes.forEach { i ->
                    writer.appendLine(csvRow(
                        i.id.toString(), i.title, i.amount.toString(), i.currency,
                        i.source.displayName, DateUtils.formatISO(i.date),
                        i.notes, i.tags.joinToString(";")
                    ))
                }
            }
        } catch (e: IOException) {
            throw RuntimeException("Failed to export CSV: ${e.message}", e)
        }
        return file
    }

    fun exportExpensesPDF(
        expenses: List<Expense>,
        currency: String,
        title: String = "Expense Report",
        filename: String = "expenses_${Date().time}.pdf"
    ): File {
        val file = File(getExportDir(), filename)
        val pdfDocument = PdfDocument()

        val paint = Paint().apply { textSize = 12f; color = Color.BLACK }
        val titlePaint = Paint().apply { textSize = 18f; color = Color.BLACK; isFakeBoldText = true }
        val headerPaint = Paint().apply { textSize = 11f; color = Color.WHITE; isFakeBoldText = true }
        val subPaint = Paint().apply { textSize = 10f; color = Color.DKGRAY }
        val bgPaint = Paint().apply { color = Color.rgb(26, 35, 126) }
        val rowBgPaint = Paint().apply { color = Color.rgb(240, 240, 250) }

        val pageWidth = 595; val pageHeight = 842
        val margin = 40f; val rowHeight = 30f

        var pageNum = 1
        var currentY = margin + 60f
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        fun newPage() {
            pdfDocument.finishPage(page)
            pageNum++
            currentY = margin + 20f
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
        }

        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 70f, bgPaint)
        canvas.drawText(title, margin, 30f, titlePaint.apply { color = Color.WHITE })
        canvas.drawText("Generated: ${DateUtils.formatFull(Date())}", margin, 55f, subPaint.apply { color = Color.LTGRAY })
        currentY = 90f

        val total = expenses.sumOf { it.amount }
        canvas.drawText("Total: ${CurrencyUtils.format(total, currency)}  |  Transactions: ${expenses.size}", margin, currentY, subPaint)
        currentY += 25f

        canvas.drawRect(margin, currentY, pageWidth - margin, currentY + rowHeight, bgPaint)
        canvas.drawText("Date", margin + 5, currentY + 20f, headerPaint)
        canvas.drawText("Title", margin + 75, currentY + 20f, headerPaint)
        canvas.drawText("Category", margin + 220, currentY + 20f, headerPaint)
        canvas.drawText("Amount", margin + 360, currentY + 20f, headerPaint)
        canvas.drawText("Method", margin + 450, currentY + 20f, headerPaint)
        currentY += rowHeight + 5f

        expenses.forEachIndexed { index, expense ->
            if (currentY + rowHeight > pageHeight - margin) newPage()
            if (index % 2 == 0) canvas.drawRect(margin, currentY - 5f, pageWidth - margin, currentY + rowHeight - 5f, rowBgPaint)
            val rowY = currentY + 15f
            paint.textSize = 10f; paint.color = Color.BLACK
            canvas.drawText(DateUtils.formatDisplay(expense.date), margin + 5, rowY, paint)
            canvas.drawText(expense.title.take(20), margin + 75, rowY, paint)
            canvas.drawText(expense.category.displayName.take(15), margin + 220, rowY, paint)
            canvas.drawText(CurrencyUtils.format(expense.amount, currency), margin + 360, rowY, paint)
            canvas.drawText(expense.paymentMethod.displayName.take(10), margin + 450, rowY, paint)
            currentY += rowHeight
        }

        pdfDocument.finishPage(page)
        pdfDocument.writeTo(file.outputStream())
        pdfDocument.close()
        return file
    }

    fun getExportedFiles(): List<File> =
        getExportDir().listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
}
