package com.safeguard.android

/** Informational frequency summary, not a malware detector. Uses only local package/time metadata. */
internal data class FrequentNotificationSource(val packageName: String, val count: Int)

internal object NotificationFrequencyAnalyzer {
    const val WINDOW_MILLIS = 15L * 60L * 1000L
    const val NOTICE_THRESHOLD = 5

    fun recentSources(events: List<NotificationEvent>, nowMillis: Long): List<FrequentNotificationSource> {
        val start = nowMillis - WINDOW_MILLIS
        return events.asSequence()
            .filter { it.packageName.isNotBlank() && it.postedAtMillis in start..nowMillis }
            .groupingBy { it.packageName }
            .eachCount()
            .map { (packageName, count) -> FrequentNotificationSource(packageName, count) }
            .sortedWith(compareByDescending<FrequentNotificationSource> { it.count }.thenBy { it.packageName })
    }
}
