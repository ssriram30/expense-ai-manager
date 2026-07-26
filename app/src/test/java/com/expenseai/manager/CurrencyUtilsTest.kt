package com.expenseai.manager

import com.expenseai.manager.util.CurrencyUtils
import org.junit.Assert.*
import org.junit.Test

class CurrencyUtilsTest {

    @Test
    fun `format MYR returns RM prefix`() {
        val result = CurrencyUtils.format(100.50, "MYR")
        assertTrue(result.contains("RM"))
        assertTrue(result.contains("100.50"))
    }

    @Test
    fun `format INR returns rupee symbol`() {
        val result = CurrencyUtils.format(1000.0, "INR")
        assertTrue(result.contains("₹"))
    }

    @Test
    fun `getSymbol returns correct symbols`() {
        assertEquals("RM", CurrencyUtils.getSymbol("MYR"))
        assertEquals("₹", CurrencyUtils.getSymbol("INR"))
        assertEquals("$", CurrencyUtils.getSymbol("USD"))
        assertEquals("€", CurrencyUtils.getSymbol("EUR"))
    }

    @Test
    fun `convert same currency returns same amount`() {
        val result = CurrencyUtils.convert(100.0, "MYR", "MYR", 1.0)
        assertEquals(100.0, result, 0.01)
    }

    @Test
    fun `convert MYR to INR uses rate`() {
        val result = CurrencyUtils.convert(100.0, "MYR", "INR", 18.5)
        assertEquals(1850.0, result, 0.01)
    }

    @Test
    fun `getCurrencyFlag returns flag emoji`() {
        assertEquals("🇲🇾", CurrencyUtils.getCurrencyFlag("MYR"))
        assertEquals("🇮🇳", CurrencyUtils.getCurrencyFlag("INR"))
    }
}
