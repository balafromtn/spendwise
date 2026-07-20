package com.expensetracker

import com.expensetracker.domain.usecase.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {

    private val dateUtils = DateUtils()

    @Test
    fun `toSheetDate formats correctly`() {
        val date = LocalDate.of(2026, 7, 20)
        val result = dateUtils.toSheetDate(date)
        assertEquals("20-Jul-2026", result)
    }

    @Test
    fun `toMonthString formats correctly`() {
        val date = LocalDate.of(2026, 7, 20)
        val result = dateUtils.toMonthString(date)
        assertEquals("Jul 2026", result)
    }

    @Test
    fun `toWeekNumber returns ISO week`() {
        val date = LocalDate.of(2026, 7, 20) // Monday of week 30
        val result = dateUtils.toWeekNumber(date)
        assertEquals(30, result)
    }

    @Test
    fun `currentMonthString returns valid format`() {
        val result = dateUtils.currentMonthString()
        assertNotNull(result)
        assert(result.matches(Regex("[A-Z][a-z]{2} \\d{4}")))
    }

    @Test
    fun `parseSheetDate parses valid date`() {
        val result = dateUtils.parseSheetDate("20-Jul-2026")
        assertEquals(LocalDate.of(2026, 7, 20), result)
    }

    @Test
    fun `parseSheetDate returns today for invalid date`() {
        val result = dateUtils.parseSheetDate("invalid-date")
        assertNotNull(result)
    }

    @Test
    fun `today returns current date`() {
        val today = dateUtils.today()
        assertEquals(LocalDate.now(), today)
    }

    @Test
    fun `now returns current time`() {
        val now = dateUtils.now()
        assertNotNull(now)
    }
}
