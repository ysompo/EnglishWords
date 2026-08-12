package com.ysompo.englishwords.logic

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UpdateVersionComparatorTest {

    @Test
    fun `higher minor version is newer`() {
        assertThat(UpdateVersionComparator.isNewer("1.2.0", "1.1.0")).isTrue()
    }

    @Test
    fun `same version is not newer`() {
        assertThat(UpdateVersionComparator.isNewer("1.1.0", "1.1.0")).isFalse()
    }

    @Test
    fun `leading v prefix on the remote tag is ignored`() {
        assertThat(UpdateVersionComparator.isNewer("v1.1.0", "1.1.0")).isFalse()
        assertThat(UpdateVersionComparator.isNewer("v2.0.0", "1.1.0")).isTrue()
    }

    @Test
    fun `different segment counts are compared correctly`() {
        assertThat(UpdateVersionComparator.isNewer("1.1", "1.1.0")).isFalse()
        assertThat(UpdateVersionComparator.isNewer("1.1.1", "1.1")).isTrue()
        assertThat(UpdateVersionComparator.isNewer("2.0", "1.9.9")).isTrue()
    }

    @Test
    fun `lower version is not newer`() {
        assertThat(UpdateVersionComparator.isNewer("1.0.0", "1.1.0")).isFalse()
    }

    @Test
    fun `malformed version strings do not crash and are treated as not newer`() {
        assertThat(UpdateVersionComparator.isNewer("not-a-version", "1.1.0")).isFalse()
    }
}
