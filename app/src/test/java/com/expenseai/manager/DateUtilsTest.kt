package com.expenseai.manager

import com.expenseai.manager.util.DateUtils
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class DateUtilsTest {

    @Test
    fun `getCurrentMonth returns valid month`() {
        val month = DateUtils.getCurrentMonth()
        assertTrue(month in 1..12)
    }

    @Test
    fun `getCurrentYear returns reasonable year`() {
        val year = DateUtils.getCurrentYear()
        assertTrue(year >= 2024)
    }

    @Test
    fun `getStartOfMonth returns first day`() {
        val start = DateUtils.getStartOfMonth(1, 2024)
        val cal = Calendar.getInstance().apply { time = start }
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, cal.get(Calendar.MONTH))
        assertEquals(2024, cal.get(Calendar.YEAR))
    }

    @Test
    fun `getEndOfMonth returns last day`() {
        val end = DateUtils.getEndOfMonth(2, 2024)
        val cal = Calendar.getInstance().apply { time = end }
        assertEquals(29, cal.get(Calendar.DAY_OF_MONTH)) // 2024 is leap year
    }

    @Test
    fun `getLast12Months returns 12 months`() {
        val months = DateUtils.getLast12Months()
        assertEquals(12, months.size)
    }

    @Test
    fun `isSameMonth returns true for same month`() {
        val start = DateUtils.getStartOfMonth(1, 2024)
        val end = DateUtils.getEndOfMonth(1, 2024)
        assertTrue(DateUtils.isSameMonth(start, end))
    }

    @Test
    fun `isSameMonth returns false for different months`() {
        val jan = DateUtils.getStartOfMonth(1, 2024)
        val feb = DateUtils.getStartOfMonth(2, 2024)
        assertFalse(DateUtils.isSameMonth(jan, feb))
    }
}
