package com.example.facebook.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeUtilTest {

    @Test
    fun `toTime formats under one minute`() {
        assertEquals("00:05", 5L.toTime())
        assertEquals("00:00", 0L.toTime())
    }

    @Test
    fun `toTime formats minutes and seconds`() {
        assertEquals("01:05", 65L.toTime()) // 1m5s
        assertEquals("10:00", (10 * 60L).toTime())
    }

    @Test
    fun `toTime formats hours minutes seconds`() {
        assertEquals("01:01:01", 3661L.toTime())
        assertEquals("02:00:00", (2 * 3600L).toTime())
    }

    @Test
    fun `parseDate returns formatted date or empty on error`() {
        val dateStr = "2024-01-01T12:00:00.000Z"
        val formatted = parseDate(dateStr, "dd/MM/yyyy")
        assertEquals("01/01/2024", formatted)

        val invalid = parseDate("not-a-date", "dd/MM/yyyy")
        assertEquals("", invalid)
    }

    @Test
    fun `toTime should not produce negative components (TODO fix)`() {
        // Current behavior yields "00:-1" for -1L, which is undesirable.
        // Preferred: clamp to "00:00" or throw.
        assertEquals("00:00", (-1L).toTime())
    }
}
