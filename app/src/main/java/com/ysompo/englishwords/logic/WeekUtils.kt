package com.ysompo.englishwords.logic

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object WeekUtils {
    private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun weekStartFor(date: LocalDate): LocalDate {
        // DayOfWeek.value: Monday=1 .. Sunday=7. Sunday%7=0 (no shift), Monday%7=1, ... Saturday%7=6.
        val daysSinceSunday = date.dayOfWeek.value % 7
        return date.minusDays(daysSinceSunday.toLong())
    }

    fun isSchoolDay(date: LocalDate): Boolean =
        date.dayOfWeek != DayOfWeek.FRIDAY && date.dayOfWeek != DayOfWeek.SATURDAY

    fun formatDate(date: LocalDate): String = date.format(FORMATTER)

    fun parseDate(text: String): LocalDate = LocalDate.parse(text, FORMATTER)
}
