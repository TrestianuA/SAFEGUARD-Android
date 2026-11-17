package com.safeguard.android

import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings

/**
 * Local-only first-pass inventory. This is a configuration review, not an antivirus engine.
 * It does not read app data, messages, photos, browsing history, or calendar contents.
 */
class SecurityScanner(private val context: Context) {
    private val packageManager = context.packageManager

    private val sensitivePermissions = linkedMapOf(
        "android.permission.CAMERA" to "Cameră",
        "android.permission.RECORD_AUDIO" to "Microfon",
        "android.permission.ACCESS_FINE_LOCATION" to "Locație precisă",
        "android.permission.ACCESS_COARSE_LOCATION" to "Locație aproximativă",
        "android.permission.ACCESS_BACKGROUND_LOCATION" to "Locație în fundal",
        "android.permission.READ_CONTACTS" to "Contacte",
        "android.permission.WRITE_CONTACTS" to "Modificare contacte",
        "android.permission.READ_SMS" to "Citire SMS",
        "android.permission.RECEIVE_SMS" to "Primire SMS",
        "android.permission.RECEIVE_MMS" to "Primire MMS",
        "android.permission.RECEIVE_WAP_PUSH" to "Primire mesaje WAP Push",
        "android.permission.SEND_SMS" to "Trimitere SMS",
        "android.permission.READ_CALENDAR" to "Citire calendar",
        "android.permission.WRITE_CALENDAR" to "Modificare calendar",
        "android.permission.READ_CALL_LOG" to "Istoric apeluri",
        "android.permission.WRITE_CALL_LOG" to "Modificare istoric apeluri",
        "android.permission.READ_PHONE_STATE" to "Stare telefon",
        "android.permission.READ_PHONE_NUMBERS" to "Numere de telefon",
        "android.permission.CALL_PHONE" to "Inițiere apeluri",
        "android.permission.ANSWER_PHONE_CALLS" to "Preluare apeluri",
        "android.permission.BODY_SENSORS" to "Senzori corporali",
        "android.permission.BODY_SENSORS_BACKGROUND" to "Senzori corporali în fundal",
        "android.permission.ACTIVITY_RECOGNITION" to "Recunoașterea activității fizice",
        "android.permission.BLUETOOTH_SCAN" to "Scanare dispozitive Bluetooth",
        "android.permission.BLUETOOTH_CONNECT" to "Conectare dispozitive Bluetooth",
        "android.permission.BLUETOOTH_ADVERTISE" to "Detectabilitate Bluetooth",
        "android.permission.NEARBY_WIFI_DEVICES" to "Dispozitive Wi-Fi din apropiere",
        "android.permission.READ_EXTERNAL_STORAGE" to "Citire stocare partajată (vechi)",
        "android.permission.WRITE_EXTERNAL_STORAGE" to "Modificare stocare partajată (vechi)",
        "android.permission.READ_MEDIA_IMAGES" to "Fotografii",
        "android.permission.READ_MEDIA_VIDEO" to "Videoclipuri",
        "android.permission.READ_MEDIA_AUDIO" to "Fișiere audio",
        "android.permission.READ_MEDIA_VISUAL_USER_SELECTED" to "Fotografii/video selectate",
        // This permission allows an app to post notifications; it does NOT grant access to read them.
        "android.permission.POST_NOTIFICATIONS" to "Trimitere notificări (nu citire)"
    )

    fun scan(): ScanReport {
        val apps = getInstalledApplications()
        val thirdPartyApps = apps.filter { app ->
            app.packageName != context.packageName && !app.isSystemApp()
        }

        val permissionApps = thirdPartyApps.mapNotNull { app ->
            val granted = sensitivePermissions.mapNotNull { (permission, label) ->
                if (packageManager.checkPermission(permission, app.packageName) == PackageManager.PERMISSION_GRANTED) {
                    label
                } else {
                    null
                }
            }
            if (granted.isEmpty()) null else PermissionApp(
                appLabel = app.label(),
                packageName = app.packageName,
                grantedPermissions = granted
            )
        }.sortedBy { it.appLabel.lowercase() }

        val specialAccess = buildList {
            addEnabledComponents(
                settingValue = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                ),
                category = "Serviciu Accessibility activ",
                explanation = "Un serviciu Accessibility poate observa sau interacționa cu interfața. Unele servicii legitime au nevoie de acest acces.",
                target = SettingsTarget.ACCESSIBILITY
            )

            addEnabledComponents(
                settingValue = Settings.Secure.getString(
                    context.contentResolver,
                    "enabled_notification_listeners"
                ),
                category = "Acces la notificări activ",
                explanation = "Aplicația poate primi notificările afișate de sistem. Verifică dacă recunoști aplicația și motivul accesului.",
                target = SettingsTarget.NOTIFICATION_LISTENERS
            )

            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val activeAdmins = try {
                devicePolicyManager.activeAdmins.orEmpty()
            } catch (_: Exception) {
                emptyList()
            }
            activeAdmins.forEach { component ->
                val pkg = component.packageName
                if (pkg != context.packageName) {
                    add(
                        SpecialAccessItem(
                            category = "Administrator dispozitiv activ",
                            appLabel = appLabel(pkg),
                            packageName = pkg,
                            explanation = "Administratorii dispozitivului pot avea drepturi speciale de administrare. Nu dezactiva un profil de serviciu sau de securitate fără să știi ce controlează.",
                            settingsTarget = SettingsTarget.DEVICE_SECURITY
                        )
                    )
                }
            }

            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            thirdPartyApps.forEach { app ->
                if (isOpAllowed(appOps, AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, app)) {
                    add(
                        SpecialAccessItem(
                            category = "Afișare peste alte aplicații",
                            appLabel = app.label(),
                            packageName = app.packageName,
                            explanation = "Permite afișarea de ferestre peste alte aplicații. Este folosit și legitim de unele aplicații; verifică dacă îl recunoști.",
                            settingsTarget = SettingsTarget.OVERLAY
                        )
                    )
                }
                if (isOpAllowed(appOps, "android:request_install_packages", app)) {
                    add(
                        SpecialAccessItem(
                            category = "Permisiune de solicitare instalare APK",
                            appLabel = app.label(),
                            packageName = app.packageName,
                            explanation = "Aplicația poate solicita instalarea altor pachete. Această setare nu înseamnă că a instalat ceva fără acord.",
                            settingsTarget = SettingsTarget.UNKNOWN_APP_SOURCES
                        )
                    )
                }
                if (Build.VERSION.SDK_INT >= 30 && isOpAllowed(appOps, "android:manage_external_storage", app)) {
                    add(
                        SpecialAccessItem(
                            category = "Acces la toate fișierele",
                            appLabel = app.label(),
                            packageName = app.packageName,
                            explanation = "Aplicația poate gestiona o mare parte din fișierele partajate. Unele aplicații de fișiere sau backup au motive legitime; verifică dacă recunoști aplicația.",
                            settingsTarget = SettingsTarget.ALL_FILES_ACCESS
                        )
                    )
                }
                if (isOpAllowed(appOps, AppOpsManager.OPSTR_GET_USAGE_STATS, app)) {
                    add(
                        SpecialAccessItem(
                            category = "Acces la datele de utilizare",
                            appLabel = app.label(),
                            packageName = app.packageName,
                            explanation = "Aplicația poate vedea informații despre utilizarea altor aplicații. Accesul poate fi legitim pentru control parental, automatizare sau monitorizare acceptată.",
                            settingsTarget = SettingsTarget.USAGE_ACCESS
                        )
                    )
                }
            }
        }.sortedWith(compareBy<SpecialAccessItem> { it.category }.thenBy { it.appLabel.lowercase() })

        return ScanReport(
            deviceModel = listOf(Build.MANUFACTURER, Build.MODEL).joinToString(" ").trim(),
            androidRelease = Build.VERSION.RELEASE ?: "Necunoscut",
            sdkInt = Build.VERSION.SDK_INT,
            securityPatch = Build.VERSION.SECURITY_PATCH ?: "Necunoscut",
            installedApps = apps.size,
            userApps = thirdPartyApps.size,
            permissionApps = permissionApps,
            specialAccess = specialAccess
        )
    }

    @Suppress("DEPRECATION")
    private fun getInstalledApplications(): List<ApplicationInfo> {
        return if (Build.VERSION.SDK_INT >= 33) {
            packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0L))
        } else {
            packageManager.getInstalledApplications(0)
        }
    }

    private fun ApplicationInfo.isSystemApp(): Boolean =
        (flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
            (flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

    private fun ApplicationInfo.label(): String = try {
        packageManager.getApplicationLabel(this).toString().ifBlank { packageName }
    } catch (_: Exception) {
        packageName
    }

    private fun appLabel(packageName: String): String = try {
        val info = if (Build.VERSION.SDK_INT >= 33) {
            packageManager.getApplicationInfo(
                packageName,
                PackageManager.ApplicationInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getApplicationInfo(packageName, 0)
        }
        packageManager.getApplicationLabel(info).toString().ifBlank { packageName }
    } catch (_: Exception) {
        packageName
    }

    private fun MutableList<SpecialAccessItem>.addEnabledComponents(
        settingValue: String?,
        category: String,
        explanation: String,
        target: SettingsTarget
    ) {
        settingValue.orEmpty()
            .split(':')
            .mapNotNull { ComponentName.unflattenFromString(it) }
            .distinctBy { it.packageName }
            .filter { it.packageName != context.packageName }
            .forEach { component ->
                add(
                    SpecialAccessItem(
                        category = category,
                        appLabel = appLabel(component.packageName),
                        packageName = component.packageName,
                        explanation = explanation,
                        settingsTarget = target
                    )
                )
            }
    }

    @Suppress("DEPRECATION")
    private fun isOpAllowed(
        appOps: AppOpsManager,
        operation: String,
        app: ApplicationInfo
    ): Boolean = try {
        val mode = if (Build.VERSION.SDK_INT >= 29) {
            appOps.unsafeCheckOpNoThrow(operation, app.uid, app.packageName)
        } else {
            appOps.checkOpNoThrow(operation, app.uid, app.packageName)
        }
        mode == AppOpsManager.MODE_ALLOWED
    } catch (_: Exception) {
        false
    }
}
