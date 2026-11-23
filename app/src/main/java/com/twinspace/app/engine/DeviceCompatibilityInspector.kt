package com.twinspace.app.engine

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.twinspace.app.model.DeviceCompatibility

/**
 * Inspects real Android API level, multi-window mode, freeform hardware feature,
 * low-RAM restrictions, and OEM-specific split-screen behavior.
 */
object DeviceCompatibilityInspector {

    private const val FEATURE_FREEFORM = "android.software.freeform_window_management"

    fun inspect(activity: Activity): DeviceCompatibility {
        val activityManager = activity.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val isLowRam = activityManager?.isLowRamDevice == true
        val supportsFreeform = activity.packageManager.hasSystemFeature(FEATURE_FREEFORM)
        val smallestWidthDp = activity.resources.configuration.smallestScreenWidthDp
        val isLargeScreen = smallestWidthDp >= 600
        val isInMultiWindow = activity.isInMultiWindowMode

        return evaluateCompatibility(
            sdkInt = Build.VERSION.SDK_INT,
            release = Build.VERSION.RELEASE ?: "Unknown",
            manufacturer = Build.MANUFACTURER ?: "Android",
            model = Build.MODEL ?: "Device",
            smallestScreenWidthDp = smallestWidthDp,
            isLowRamDevice = isLowRam,
            isCurrentlyInMultiWindowMode = isInMultiWindow,
            supportsFreeformFeature = supportsFreeform,
            isLargeScreen = isLargeScreen
        )
    }

    fun evaluateCompatibility(
        sdkInt: Int,
        release: String,
        manufacturer: String,
        model: String,
        smallestScreenWidthDp: Int,
        isLowRamDevice: Boolean,
        isCurrentlyInMultiWindowMode: Boolean,
        supportsFreeformFeature: Boolean,
        isLargeScreen: Boolean
    ): DeviceCompatibility {
        val supportsSplitScreen = sdkInt >= Build.VERSION_CODES.N && !isLowRamDevice
        val supportsDirectAdjacent = isCurrentlyInMultiWindowMode || isLargeScreen

        val osGuidance = when {
            isLowRamDevice ->
                "This device reports isLowRamDevice = true (Android Go / low-RAM profile). Android disables system split-screen on low-RAM builds."
            sdkInt >= 35 ->
                "Android 15+ (API $sdkInt): Enforces strict split-screen task pairing. Pin TwinSpace in Split Screen from Recents (or use a large-screen/foldable taskbar) so FLAG_ACTIVITY_LAUNCH_ADJACENT places the second app beside the first."
            sdkInt == Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
                "Android 14 (API 34): Supports side-by-side split pairs and app pairs. Entering Split Screen from Android Recents before tapping Open Both ensures reliable two-pane placement on phones."
            sdkInt == Build.VERSION_CODES.TIRAMISU ->
                "Android 13 (API 33): Supports native split-screen pairing. On standard phones, start Split Screen from Recents so TwinSpace occupies one pane and launches both selected apps into the two panes."
            sdkInt == Build.VERSION_CODES.S || sdkInt == Build.VERSION_CODES.S_V2 ->
                "Android 12 / 12L (API $sdkInt): Android 12+ treats split screen as unified app pairs. Pin TwinSpace in Split Screen from Recents first on phones; 12L/tablets also support taskbar drag-split."
            sdkInt == Build.VERSION_CODES.R ->
                "Android 11 (API 30): Native split-screen supported via Recents or adjacent launch while TwinSpace is in multi-window mode."
            sdkInt == Build.VERSION_CODES.Q ->
                "Android 10 (API 29): Supports native multi-resume split-screen, allowing both visible apps to remain resumed simultaneously."
            else ->
                "Android ${release} (API $sdkInt): Legacy multi-window behavior; one split pane may pause when the other receives focus."
        }

        val normalizedMfg = manufacturer.trim().lowercase()
        val oemNotes = when {
            normalizedMfg.contains("samsung") ->
                "Samsung One UI: Supports native Split Screen and App Pairs in Recents. MultiStar in Good Lock can additionally enable multi-window for stubborn apps."
            normalizedMfg.contains("xiaomi") || normalizedMfg.contains("redmi") || normalizedMfg.contains("poco") ->
                "Xiaomi MIUI / HyperOS: Split screen is accessed by long-pressing an app card in Recents and tapping the Split Screen icon. Some system apps block split view unless MIUI Optimization permits resizing."
            normalizedMfg.contains("oneplus") || normalizedMfg.contains("oppo") || normalizedMfg.contains("realme") ->
                "OnePlus / OPPO / Realme (OxygenOS / ColorOS): Open Recents -> tap the three-dot menu on TwinSpace -> Split screen. Supports flexible split ratios."
            normalizedMfg.contains("google") ->
                "Google Pixel (AOSP): Tap the app icon at the top of TwinSpace in Recents and choose 'Split top' (or 'Split screen'), then tap Open Both."
            else ->
                "$manufacturer ($model): Manufacturer Recents gestures vary. Open Android Recents and choose Split Screen / Split Top on TwinSpace if direct launch opens full-screen."
        }

        return DeviceCompatibility(
            sdkInt = sdkInt,
            androidVersionLabel = "Android $release (API $sdkInt)",
            manufacturer = manufacturer,
            model = model,
            smallestScreenWidthDp = smallestScreenWidthDp,
            isLargeScreen = isLargeScreen,
            isLowRamDevice = isLowRamDevice,
            isCurrentlyInMultiWindowMode = isCurrentlyInMultiWindowMode,
            supportsSplitScreen = supportsSplitScreen,
            supportsFreeformWindows = supportsFreeformFeature && !isLowRamDevice,
            supportsDirectAdjacentFromFullScreen = supportsDirectAdjacent,
            osVersionGuidance = osGuidance,
            oemNotes = oemNotes
        )
    }
}
