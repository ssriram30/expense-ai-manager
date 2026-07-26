package com.expenseai.manager.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyUtils {

    val SUPPORTED_CURRENCIES = listOf("MYR", "INR", "USD", "EUR", "GBP", "SGD", "AUD", "JPY", "CNY", "THB")

    private val currencySymbols = mapOf(
        "MYR" to "RM",
        "INR" to "₹",
        "USD" to "$",
        "EUR" to "€",
        "GBP" to "£",
        "SGD" to "S$",
        "AUD" to "A$",
        "JPY" to "¥",
        "CNY" to "¥",
        "THB" to "฿"
    )

    private val currencyLocales = mapOf(
        "MYR" to Locale("ms", "MY"),
        "INR" to Locale("en", "IN"),
        "USD" to Locale.US,
        "EUR" to Locale.GERMANY,
        "GBP" to Locale.UK,
        "SGD" to Locale("en", "SG"),
        "AUD" to Locale("en", "AU"),
        "JPY" to Locale.JAPAN,
        "CNY" to Locale.CHINA,
        "THB" to Locale("th", "TH")
    )

    fun getSymbol(currencyCode: String): String =
        currencySymbols[currencyCode] ?: currencyCode

    fun format(amount: Double, currencyCode: String): String {
        val symbol = getSymbol(currencyCode)
        val formatted = when (currencyCode) {
            "JPY" -> "%s %.0f".format(symbol, amount)
            "INR" -> formatIndian(amount, symbol)
            else -> "%s %.2f".format(symbol, amount)
        }
        return formatted
    }

    private fun formatIndian(amount: Double, symbol: String): String {
        val intPart = amount.toLong()
        val decPart = ((amount - intPart) * 100).toLong()
        val formatted = formatIndianNumber(intPart)
        return if (decPart > 0) "$symbol$formatted.${"%02d".format(decPart)}"
        else "$symbol$formatted"
    }

    private fun formatIndianNumber(number: Long): String {
        if (number < 1000) return number.toString()
        val s = number.toString()
        val len = s.length
        return when {
            len <= 3 -> s
            len == 4 -> s[0] + "," + s.substring(1)
            len == 5 -> s.substring(0, 2) + "," + s.substring(2)
            len == 6 -> s.substring(0, 1) + "," + s.substring(1, 3) + "," + s.substring(3)
            else -> {
                val crore = s.substring(0, len - 7)
                val lakh = s.substring(len - 7, len - 5)
                val thousand = s.substring(len - 5, len - 3)
                val hundreds = s.substring(len - 3)
                buildString {
                    if (crore.isNotEmpty()) append("$crore,")
                    if (lakh.isNotEmpty()) append("$lakh,")
                    if (thousand.isNotEmpty()) append("$thousand,")
                    append(hundreds)
                }
            }
        }
    }

    fun convert(amount: Double, fromCurrency: String, toCurrency: String, rate: Double): Double {
        if (fromCurrency == toCurrency) return amount
        return amount * rate
    }

    fun getCurrencyFlag(currencyCode: String): String = when (currencyCode) {
        "MYR" -> "🇲🇾"
        "INR" -> "🇮🇳"
        "USD" -> "🇺🇸"
        "EUR" -> "🇪🇺"
        "GBP" -> "🇬🇧"
        "SGD" -> "🇸🇬"
        "AUD" -> "🇦🇺"
        "JPY" -> "🇯🇵"
        "CNY" -> "🇨🇳"
        "THB" -> "🇹🇭"
        else -> "💱"
    }

    fun getCurrencyName(currencyCode: String): String = when (currencyCode) {
        "MYR" -> "Malaysian Ringgit"
        "INR" -> "Indian Rupee"
        "USD" -> "US Dollar"
        "EUR" -> "Euro"
        "GBP" -> "British Pound"
        "SGD" -> "Singapore Dollar"
        "AUD" -> "Australian Dollar"
        "JPY" -> "Japanese Yen"
        "CNY" -> "Chinese Yuan"
        "THB" -> "Thai Baht"
        else -> currencyCode
    }
}
