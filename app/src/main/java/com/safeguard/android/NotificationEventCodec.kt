package com.safeguard.android

import java.util.PriorityQueue

/** Metadata retained for troubleshooting notification sources; notification content is never read. */
data class NotificationEvent(
    val packageName: String,
    val postedAtMillis: Long
)

/** Pure local format so the retention and parsing rules can be unit-tested without Android APIs. */
internal object NotificationEventCodec {
    private const val MAX_EVENTS = 100

    fun encode(events: Iterable<NotificationEvent>): String = events
        .filter { it.packageName.isNotBlank() }
        .sortedBy { it.postedAtMillis }
        .takeLast(MAX_EVENTS)
        .joinToString("\n") { "${it.postedAtMillis}|${it.packageName}" }

    /**
     * Keeps only the newest 100 parsed rows while iterating. This also bounds the number of
     * NotificationEvent objects retained if local preferences are unexpectedly oversized.
     */
    fun decode(encoded: String?): List<NotificationEvent> {
        val newest = PriorityQueue<NotificationEvent>(compareBy { it.postedAtMillis })

        encoded.orEmpty().lineSequence().forEach { line ->
            val separator = line.indexOf('|')
            if (separator <= 0 || separator == line.lastIndex) return@forEach

            val timestamp = line.substring(0, separator).toLongOrNull() ?: return@forEach
            val packageName = line.substring(separator + 1)
            if (packageName.isBlank()) return@forEach

            val event = NotificationEvent(packageName, timestamp)
            if (newest.size < MAX_EVENTS) {
                newest.add(event)
            } else if (timestamp > newest.peek().postedAtMillis) {
                newest.poll()
                newest.add(event)
            }
        }

        return newest.sortedByDescending { it.postedAtMillis }
    }
}
