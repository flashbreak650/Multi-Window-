package com.twinspace.app.model

import android.content.ComponentName

/**
 * Represents a real, launchable Android application discovered via PackageManager.
 */
data class InstalledApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val isSystemApp: Boolean,
    val launchMode: Int,
    val supportsStandardMultiTask: Boolean
) {
    val componentName: ComponentName
        get() = ComponentName(packageName, activityName)
}
