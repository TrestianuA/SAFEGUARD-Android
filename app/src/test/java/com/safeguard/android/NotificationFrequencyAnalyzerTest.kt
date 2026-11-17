package com.safeguard.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationFrequencyAnalyzerTest {
    private val now = 1_700_000_000_000L

    @Test fun countsOnlyEventsWithinLastFifteenMinutes() {
        val events = listOf(
            NotificationEvent("a", now), NotificationEvent("a", now - 899_999L),
            NotificationEvent("a", now - 900_001L), NotificationEvent("b", now + 1L)
        )
        assertEquals(listOf(FrequentNotificationSource("a", 2)), NotificationFrequencyAnalyzer.recentSources(events, now))
    }

    @Test fun thresholdDoesNotClassifyAppsAsMalware() {
        val events = (1..5).map { NotificationEvent("com.example", now - it) }
        val sources = NotificationFrequencyAnalyzer.recentSources(events, now)
        assertEquals(5, sources.single().count)
        assertTrue(sources.single().count >= NotificationFrequencyAnalyzer.NOTICE_THRESHOLD)
    }

    @Test fun ordersByFrequencyThenPackage() {
        val events = listOf("b", "a", "b", "a", "c").map { NotificationEvent(it, now) }
        assertEquals(listOf("a", "b", "c"), NotificationFrequencyAnalyzer.recentSources(events, now).map { it.packageName })
    }
}
