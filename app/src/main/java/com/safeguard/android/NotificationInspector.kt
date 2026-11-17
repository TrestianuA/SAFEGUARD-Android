package com.safeguard.android

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

object NotificationAccess {
    fun isEnabled(context: Context): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ).orEmpty()
        val expected = ComponentName(context, NotificationCaptureService::class.java)
        return enabled.split(':')
            .mapNotNull { ComponentName.unflattenFromString(it) }
            .any { it == expected }
    }
}

/**
 * Keeps at most 100 local metadata records. Stored fields are only package name and timestamp.
 * No title, text, sender, action, icon, or notification extras are accessed.
 */
class NotificationLogStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun append(event: NotificationEvent) = synchronized(STORE_LOCK) {
        if (event.packageName.isBlank()) return@synchronized
        val updated = readRecent().toMutableList()
        updated.add(event)
        preferences.edit().putString(KEY_EVENTS, NotificationEventCodec.encode(updated)).apply()
    }

    fun readRecent(): List<NotificationEvent> = synchronized(STORE_LOCK) {
        NotificationEventCodec.decode(preferences.getString(KEY_EVENTS, ""))
    }

    fun clear() = synchronized(STORE_LOCK) {
        preferences.edit().remove(KEY_EVENTS).apply()
    }

    private companion object {
        const val PREFS_NAME = "notification_inspector_v1"
        const val KEY_EVENTS = "events"
        val STORE_LOCK = Any()
    }
}

/** User-enabled notification listener. It intentionally reads only source package and post time. */
class NotificationCaptureService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return
        NotificationLogStore(applicationContext).append(
            NotificationEvent(
                packageName = sbn.packageName,
                postedAtMillis = sbn.postTime
            )
        )
    }
}
