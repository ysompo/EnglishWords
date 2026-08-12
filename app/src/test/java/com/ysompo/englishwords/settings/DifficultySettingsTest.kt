package com.ysompo.englishwords.settings

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DifficultySettingsTest {

    @Test
    fun `defaults to BEGINNER when nothing was saved`() {
        val settings = DifficultySettings(ApplicationProvider.getApplicationContext())

        assertThat(settings.getLevel()).isEqualTo(ProficiencyLevel.BEGINNER)
    }

    @Test
    fun `setLevel persists and is read back`() {
        val settings = DifficultySettings(ApplicationProvider.getApplicationContext())

        settings.setLevel(ProficiencyLevel.ADVANCED)

        assertThat(settings.getLevel()).isEqualTo(ProficiencyLevel.ADVANCED)
    }
}
