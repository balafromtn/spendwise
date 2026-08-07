package com.expensetracker.domain.usecase

import android.util.Log

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields

class DateUtils {

    private val sheetDateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
    private val sheetTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

    fun now(): LocalDateTime = LocalDateTime.now()

    fun today(): LocalDate = LocalDate.now()

    fun toSheetDate(date: LocalDate): String = date.format(sheetDateFormatter)

    fun toSheetTime(time: LocalDateTime): String = time.format(sheetTimeFormatter)

    fun toMonthString(date: LocalDate): String = date.format(monthFormatter)

    fun toWeekNumber(date: LocalDate): Int = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)

    fun currentMonthString(): String = toMonthString(today())

    fun weeksElapsedInMonth(date: LocalDate): Int = ((date.dayOfMonth + 6) / 7).coerceAtLeast(1)

    fun toEpochMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun availableMonths(count: Int = 24): List<String> {
        val today = today()
        val start = today.withDayOfMonth(1).minusMonths((count - 1).toLong())
        return (0 until count).map { start.plusMonths(it.toLong()).format(monthFormatter) }
    }

    fun parseSheetDate(dateStr: String): LocalDate? {
        return try {
            LocalDate.parse(dateStr, sheetDateFormatter)
        } catch (e: Exception) {
            try {
                val parts = dateStr.split("-")
                if (parts.size == 3) {
                    val day = parts[0].toInt()
                    val month = java.time.Month.valueOf(parts[1].uppercase())
                    val year = parts[2].toInt()
                    LocalDate.of(year, month, day)
                } else {
                    Log.w("DateUtils", "Unparseable date string: $dateStr")
                    null
                }
            } catch (e2: Exception) {
                Log.w("DateUtils", "Failed to parse date: $dateStr", e2)
                null
            }
        }
    }

    fun epochMillisToLocalDateTime(millis: Long): LocalDateTime {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    fun millisToTimeString(millis: Long): String {
        return if (millis > 0) {
            epochMillisToLocalDateTime(millis).format(sheetTimeFormatter)
        } else {
            ""
        }
    }

    fun legacyDateTimeMillis(date: String, time: String): Long {
        return try {
            val d = parseSheetDate(date)
            val t = if (time.isBlank()) LocalTime.MIDNIGHT else LocalTime.parse(time)
            LocalDateTime.of(d, t).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: Exception) {
            0L
        }
    }

    fun getMonthRange(monthString: String): Pair<Long, Long> {
        val date = try {
            LocalDate.parse("01-$monthString", DateTimeFormatter.ofPattern("dd-MMM yyyy"))
        } catch (e: Exception) {
            today()
        }
        val start = toEpochMillis(date.withDayOfMonth(1))
        val end = toEpochMillis(date.withDayOfMonth(date.lengthOfMonth())) + (24 * 60 * 60 * 1000 - 1) // end of last day
        return Pair(start, end)
    }
}
