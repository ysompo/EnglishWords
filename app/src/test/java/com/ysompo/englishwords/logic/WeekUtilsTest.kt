package com.ysompo.englishwords.logic

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class WeekUtilsTest {

    @Test
    fun `weekStartFor returns the Sunday of that week for every day Sun-Sat`() {
        // 2026-08-09 is a Sunday
        val sunday = LocalDate.of(2026, 8, 9)
        assertThat(WeekUtils.weekStartFor(sunday)).isEqualTo(sunday)
        assertThat(WeekUtils.weekStartFor(LocalDate.of(2026, 8, 10))).isEqualTo(sunday) // Monday
        assertThat(WeekUtils.weekStartFor(LocalDate.of(2026, 8, 13))).isEqualTo(sunday) // Thursday
        assertThat(WeekUtils.weekStartFor(LocalDate.of(2026, 8, 14))).isEqualTo(sunday) // Friday
        assertThat(WeekUtils.weekStartFor(LocalDate.of(2026, 8, 15))).isEqualTo(sunday) // Saturday
        assertThat(WeekUtils.weekStartFor(LocalDate.of(2026, 8, 16))).isEqualTo(LocalDate.of(2026, 8, 16)) // next Sunday
    }

    @Test
    fun `isSchoolDay is true Sunday through Thursday, false Friday and Saturday`() {
        assertThat(WeekUtils.isSchoolDay(LocalDate.of(2026, 8, 9))).isTrue()   // Sun
        assertThat(WeekUtils.isSchoolDay(LocalDate.of(2026, 8, 13))).isTrue()  // Thu
        assertThat(WeekUtils.isSchoolDay(LocalDate.of(2026, 8, 14))).isFalse() // Fri
        assertThat(WeekUtils.isSchoolDay(LocalDate.of(2026, 8, 15))).isFalse() // Sat
    }

    @Test
    fun `formatDate and parseDate round-trip as ISO yyyy-MM-dd`() {
        val date = LocalDate.of(2026, 8, 9)
        val text = WeekUtils.formatDate(date)
        assertThat(text).isEqualTo("2026-08-09")
        assertThat(WeekUtils.parseDate(text)).isEqualTo(date)
    }
}
