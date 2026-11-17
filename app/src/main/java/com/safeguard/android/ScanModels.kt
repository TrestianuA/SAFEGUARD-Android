package com.safeguard.android

data class PermissionApp(
    val appLabel: String,
    val packageName: String,
    val grantedPermissions: List<String>
)

enum class SettingsTarget {
    APP_DETAILS,
    ACCESSIBILITY,
    NOTIFICATION_LISTENERS,
    DEVICE_SECURITY,
    OVERLAY,
    UNKNOWN_APP_SOURCES,
    ALL_FILES_ACCESS,
    USAGE_ACCESS
}

data class SpecialAccessItem(
    val category: String,
    val appLabel: String,
    val packageName: String,
    val explanation: String,
    val settingsTarget: SettingsTarget
)

data class ScanReport(
    val deviceModel: String,
    val androidRelease: String,
    val sdkInt: Int,
    val securityPatch: String,
    val installedApps: Int,
    val userApps: Int,
    val permissionApps: List<PermissionApp>,
    val specialAccess: List<SpecialAccessItem>
)
