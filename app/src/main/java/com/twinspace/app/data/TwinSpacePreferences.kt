package com.twinspace.app.data

import android.content.Context
import android.content.SharedPreferences
import com.twinspace.app.model.TwinSpaceSettings

/**
 * Local-only SharedPreferences store for recently selected apps and user preferences.
 * Never transmits or uploads app data off-device.
 */
class TwinSpacePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getRecentPackageNames(): List<String> {
        val raw = prefs.getString(KEY_RECENT_APPS, "").orEmpty()
        if (raw.isBlank()) return emptyList()
        return raw.split(DELIMITER).filter { it.isNotBlank() }
    }

    fun recordRecentApps(packageNames: List<String>) {
        val updated = updateRecentList(
            existing = getRecentPackageNames(),
            newlySelected = packageNames,
            maxItems = MAX_RECENT_APPS
        )
        prefs.edit().putString(KEY_RECENT_APPS, updated.joinToString(DELIMITER)).apply()
    }

    fun clearRecentApps() {
        prefs.edit().remove(KEY_RECENT_APPS).apply()
    }

    fun getSettings(): TwinSpaceSettings {
        return TwinSpaceSettings(
            allowSameAppTwice = prefs.getBoolean(KEY_ALLOW_SAME_APP, false),
            preferFreeformWhenSupported = prefs.getBoolean(KEY_PREFER_FREEFORM, false),
            showPrivacyDisclosureOnStart = prefs.getBoolean(KEY_SHOW_PRIVACY_DISCLOSURE, true)
        )
    }

    fun saveSettings(settings: TwinSpaceSettings) {
        prefs.edit()
            .putBoolean(KEY_ALLOW_SAME_APP, settings.allowSameAppTwice)
            .putBoolean(KEY_PREFER_FREEFORM, settings.preferFreeformWhenSupported)
            .putBoolean(KEY_SHOW_PRIVACY_DISCLOSURE, settings.showPrivacyDisclosureOnStart)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "twinspace_prefs"
        private const val KEY_RECENT_APPS = "recent_apps"
        private const val KEY_ALLOW_SAME_APP = "allow_same_app_twice"
        private const val KEY_PREFER_FREEFORM = "prefer_freeform"
        private const val KEY_SHOW_PRIVACY_DISCLOSURE = "show_privacy_disclosure"
        private const val DELIMITER = "|"
        private const val MAX_RECENT_APPS = 10

        fun updateRecentList(
            existing: List<String>,
            newlySelected: List<String>,
            maxItems: Int = MAX_RECENT_APPS
        ): List<String> {
            val result = ArrayList<String>()
            for (pkg in newlySelected) {
                if (pkg.isNotBlank() && !result.contains(pkg)) {
                    result.add(pkg)
                }
            }
            for (pkg in existing) {
                if (pkg.isNotBlank() && !result.contains(pkg)) {
                    result.add(pkg)
                }
            }
            return result.take(maxItems)
        }
    }
}
