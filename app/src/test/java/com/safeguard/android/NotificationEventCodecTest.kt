package com.safeguard.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationEventCodecTest {
    @Test
    fun keepsOnlyTheNewestOneHundredEvents() {
        val input = (1L..105L).map { NotificationEvent("com.example.app$it", it) }

        val decoded = NotificationEventCodec.decode(NotificationEventCodec.encode(input))

        assertEquals(100, decoded.size)
        assertEquals(105L, decoded.first().postedAtMillis)
        assertEquals(6L, decoded.last().postedAtMillis)
    }

    @Test
    fun decodeAlsoCapsStoredRowsAtOneHundred() {
        val encoded = (1L..150L).joinToString("\n") { "$it|com.example.app$it" }

        val decoded = NotificationEventCodec.decode(encoded)

        assertEquals(100, decoded.size)
        assertEquals(150L, decoded.first().postedAtMillis)
        assertEquals(51L, decoded.last().postedAtMillis)
    }

    @Test
    fun ignoresMalformedRowsAndSortsNewestFirst() {
        val decoded = NotificationEventCodec.decode(
            "broken-row\n200|com.example.new\nnot-a-time|com.example.bad\n100|com.example.old\n300|"
        )

        assertEquals(listOf("com.example.new", "com.example.old"), decoded.map { it.packageName })
        assertTrue(decoded.first().postedAtMillis > decoded.last().postedAtMillis)
    }

    @Test
    fun roundTripsPackageAndTimestampWithoutNotificationContent() {
        val original = listOf(
            NotificationEvent("com.example.mail", 1_700_000_000_000L),
            NotificationEvent("com.example.calendar", 1_700_000_100_000L)
        )

        val decoded = NotificationEventCodec.decode(NotificationEventCodec.encode(original))

        assertEquals(original.reversed(), decoded)
    }
}
