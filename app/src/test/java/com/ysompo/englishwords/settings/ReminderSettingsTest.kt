package com.ysompo.englishwords.settings

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReminderSettingsTest {

    @Test
    fun `defaults to 20_00 when nothing was saved`() {
        val settings = ReminderSettings(ApplicationProvider.getApplicationContext())

        assertThat(settings.getReminderHour()).isEqualTo(20)
        assertThat(settings.getReminderMinute()).isEqualTo(0)
    }

    @Test
    fun `setReminderTime persists and is read back`() {
        val settings = ReminderSettings(ApplicationProvider.getApplicationContext())

        settings.setReminderTime(hour = 18, minute = 30)

        assertThat(settings.getReminderHour()).isEqualTo(18)
        assertThat(settings.getReminderMinute()).isEqualTo(30)
    }
}
