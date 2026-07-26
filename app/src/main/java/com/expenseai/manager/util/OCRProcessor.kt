package com.expenseai.manager.util

import android.net.Uri
import com.expenseai.manager.domain.model.ExpenseCategory
import com.expenseai.manager.domain.model.PaymentMethod
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class OCRResult(
    val merchantName: String = "",
    val amount: Double = 0.0,
    val currency: String = "MYR",
    val date: Date = Date(),
    val taxAmount: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val rawText: String = ""
)

class OCRProcessor {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun processImage(image: InputImage): OCRResult =
        suspendCancellableCoroutine { cont ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    cont.resume(parseReceiptText(visionText.text))
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }

    private fun parseReceiptText(rawText: String): OCRResult {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val text = rawText.uppercase(Locale.ROOT)

        return OCRResult(
            merchantName = extractMerchantName(lines),
            amount = extractAmount(rawText),
            currency = extractCurrency(text),
            date = extractDate(rawText) ?: Date(),
            taxAmount = extractTax(rawText),
            paymentMethod = extractPaymentMethod(text),
            category = inferCategory(text),
            rawText = rawText
        )
    }

    private fun extractMerchantName(lines: List<String>): String {
        // Merchant is usually in the first 3 lines, longer than 3 chars, not all digits
        return lines.take(5)
            .filter { it.length > 3 && !it.all { c -> c.isDigit() || c == '.' || c == '/' || c == '-' } }
            .firstOrNull()?.take(50) ?: ""
    }

    private fun extractAmount(text: String): Double {
        val patterns = listOf(
            Regex("(?:TOTAL|AMOUNT|GRAND TOTAL|NET TOTAL|DUE|PAY)[:\\s]*(?:RM|MYR|INR|₹|\\$|USD)?\\s*([0-9,]+\\.?[0-9]{0,2})", RegexOption.IGNORE_CASE),
            Regex("RM\\s*([0-9,]+\\.?[0-9]{0,2})"),
            Regex("₹\\s*([0-9,]+\\.?[0-9]{0,2})"),
            Regex("([0-9,]+\\.[0-9]{2})\\s*$", RegexOption.MULTILINE)
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val amountStr = match.groupValues.drop(1).firstOrNull { it.isNotBlank() }
                    ?.replace(",", "") ?: continue
                return amountStr.toDoubleOrNull() ?: continue
            }
        }

        // Fallback: find the largest decimal number
        val allAmounts = Regex("([0-9]+\\.[0-9]{2})").findAll(text)
            .mapNotNull { it.groupValues[1].toDoubleOrNull() }
            .filter { it > 0.5 }
            .toList()
        return allAmounts.maxOrNull() ?: 0.0
    }

    private fun extractCurrency(text: String): String = when {
        text.contains("RM") || text.contains("MYR") || text.contains("RINGGIT") -> "MYR"
        text.contains("INR") || text.contains("₹") || text.contains("RUPEE") -> "INR"
        text.contains("SGD") || text.contains("S$") -> "SGD"
        text.contains("USD") || text.contains("$") -> "USD"
        text.contains("GBP") || text.contains("£") -> "GBP"
        else -> "MYR"
    }

    private fun extractDate(text: String): Date? {
        val patterns = listOf(
            Regex("(\\d{2})[/-](\\d{2})[/-](\\d{4})"),
            Regex("(\\d{4})[/-](\\d{2})[/-](\\d{2})"),
            Regex("(\\d{1,2})\\s+(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)\\s+(\\d{4})", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(text) ?: continue
            try {
                return when (patterns.indexOf(pattern)) {
                    0 -> {
                        val (day, month, year) = match.destructured
                        java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .parse("$day/$month/$year")
                    }
                    1 -> {
                        val (year, month, day) = match.destructured
                        java.text.SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                            .parse("$year/$month/$day")
                    }
                    2 -> {
                        val (day, month, year) = match.destructured
                        java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            .parse("$day $month $year")
                    }
                    else -> null
                }
            } catch (_: Exception) {}
        }
        return null
    }

    private fun extractTax(text: String): Double {
        val pattern = Regex("(?:GST|SST|TAX|VAT)[:\\s]*(?:RM|MYR|₹)?\\s*([0-9]+\\.?[0-9]{0,2})", RegexOption.IGNORE_CASE)
        return pattern.find(text)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
    }

    private fun extractPaymentMethod(text: String): PaymentMethod = when {
        text.contains("CASH") -> PaymentMethod.CASH
        text.contains("VISA") || text.contains("MASTERCARD") || text.contains("CREDIT") -> PaymentMethod.CREDIT_CARD
        text.contains("DEBIT") -> PaymentMethod.DEBIT_CARD
        text.contains("TOUCH N GO") || text.contains("GRAB PAY") || text.contains("BOOST") ||
        text.contains("E-WALLET") || text.contains("EWALLET") -> PaymentMethod.E_WALLET
        text.contains("TRANSFER") || text.contains("ONLINE") -> PaymentMethod.BANK_TRANSFER
        text.contains("UPI") || text.contains("BHIM") || text.contains("PHONEPE") ||
        text.contains("GPAY") || text.contains("PAYTM") -> PaymentMethod.UPI
        else -> PaymentMethod.CASH
    }

    private fun inferCategory(text: String): ExpenseCategory = when {
        text.any { w -> listOf("RESTAURANT", "CAFE", "FOOD", "KITCHEN", "BAKERY", "PIZZA", "BURGER", "NOODLE", "RICE", "COFFEE").any { text.contains(it) } } ->
            ExpenseCategory.FOOD_DINING
        text.any { listOf("GRAB", "TAXI", "PETROL", "PARKING", "TOLL", "BUS", "TRAIN", "MRT", "LRT").any { w -> text.contains(w) } } ->
            ExpenseCategory.TRANSPORT
        text.any { listOf("SUPERMARKET", "GROCERY", "MART", "AEON", "TESCO", "GIANT", "MYDIN").any { w -> text.contains(w) } } ->
            ExpenseCategory.GROCERIES
        text.any { listOf("PHARMACY", "CLINIC", "HOSPITAL", "DOCTOR", "MEDICAL", "HEALTH").any { w -> text.contains(w) } } ->
            ExpenseCategory.HEALTH_FITNESS
        text.any { listOf("CINEMA", "MOVIE", "ENTERTAINMENT", "CONCERT", "BOWLING").any { w -> text.contains(w) } } ->
            ExpenseCategory.ENTERTAINMENT
        text.any { listOf("HOTEL", "RESORT", "FLIGHT", "AIRASIA", "MAS", "TRAVEL").any { w -> text.contains(w) } } ->
            ExpenseCategory.TRAVEL
        text.any { listOf("TNB", "UNIFI", "MAXIS", "CELCOM", "TELCO", "ELECTRIC", "WATER", "INTERNET").any { w -> text.contains(w) } } ->
            ExpenseCategory.BILLS_UTILITIES
        text.any { listOf("BOOK", "COURSE", "SCHOOL", "UNIVERSITY", "TUITION").any { w -> text.contains(w) } } ->
            ExpenseCategory.EDUCATION
        else -> ExpenseCategory.OTHER
    }

    fun release() { recognizer.close() }
}
