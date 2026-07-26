package com.expenseai.manager.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val fullFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun formatDisplay(date: Date): String = displayFormat.format(date)
    fun formatFull(date: Date): String = fullFormat.format(date)
    fun formatMonthYear(date: Date): String = monthYearFormat.format(date)
    fun formatMonth(date: Date): String = monthFormat.format(date)
    fun formatDay(date: Date): String = dayFormat.format(date)
    fun formatISO(date: Date): String = isoFormat.format(date)

    fun getStartOfMonth(month: Int, year: Int): Date {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    fun getEndOfMonth(month: Int, year: Int): Date {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 23, 59, 59)
        cal.set(Calendar.MILLISECOND, 999)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        return cal.time
    }

    fun getStartOfYear(year: Int): Date {
        val cal = Calendar.getInstance()
        cal.set(year, 0, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    fun getEndOfYear(year: Int): Date {
        val cal = Calendar.getInstance()
        cal.set(year, 11, 31, 23, 59, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.time
    }

    fun getCurrentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

    fun getMonthName(month: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, month - 1)
        return monthFormat.format(cal.time)
    }

    fun getFullMonthName(month: Int): String {
        val sdf = SimpleDateFormat("MMMM", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, month - 1)
        return sdf.format(cal.time)
    }

    fun daysAgo(days: Int): Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return cal.time
    }

    fun weeksAgo(weeks: Int): Date = daysAgo(weeks * 7)

    fun monthsAgo(months: Int): Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -months)
        return cal.time
    }

    fun isSameMonth(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
    }

    fun addDays(date: Date, days: Int): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DAY_OF_YEAR, days)
        return cal.time
    }

    fun getDayOfMonth(date: Date): Int {
        val cal = Calendar.getInstance()
        cal.time = date
        return cal.get(Calendar.DAY_OF_MONTH)
    }

    fun getMonth(date: Date): Int {
        val cal = Calendar.getInstance()
        cal.time = date
        return cal.get(Calendar.MONTH) + 1
    }

    fun getYear(date: Date): Int {
        val cal = Calendar.getInstance()
        cal.time = date
        return cal.get(Calendar.YEAR)
    }

    fun getLast12Months(): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        val cal = Calendar.getInstance()
        repeat(12) {
            result.add(0, Pair(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)))
            cal.add(Calendar.MONTH, -1)
        }
        return result
    }
}
