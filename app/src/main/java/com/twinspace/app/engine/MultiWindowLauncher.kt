package com.twinspace.app.engine

import android.app.Activity
import android.app.ActivityOptions
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import com.twinspace.app.model.DeviceCompatibility
import com.twinspace.app.model.InstalledApp
import com.twinspace.app.model.LaunchResult
import com.twinspace.app.model.TwinSpaceSettings

/**
 * Executes genuine Android multi-window Intent launches without fake overlays,
 * WebViews, or Accessibility Service workarounds.
 */
class MultiWindowLauncher {

    fun validateSelection(
        firstApp: InstalledApp?,
        secondApp: InstalledApp?,
        settings: TwinSpaceSettings
    ): String? {
        if (firstApp == null || secondApp == null) {
            return "Select both a First App and a Second App before launching."
        }
        if (firstApp.packageName == secondApp.packageName &&
            firstApp.activityName == secondApp.activityName &&
            !settings.allowSameAppTwice
        ) {
            return "First App and Second App are identical. Enable 'Allow same app twice' in Settings if your device supports multi-instance windows."
        }
        return null
    }

    fun launchBoth(
        activity: Activity,
        firstApp: InstalledApp,
        secondApp: InstalledApp,
        compatibility: DeviceCompatibility,
        settings: TwinSpaceSettings,
        forceDirectAttempt: Boolean = false
    ): LaunchResult {
        val validationError = validateSelection(firstApp, secondApp, settings)
        if (validationError != null) {
            return LaunchResult.Failed(
                title = "Invalid App Pair",
                reason = validationError
            )
        }

        if (!compatibility.supportsSplitScreen) {
            return LaunchResult.Failed(
                title = "Split Screen Not Supported",
                reason = "This device reports low-RAM mode or an unsupported Android configuration where system Split Screen is disabled."
            )
        }

        // Verify both target components still exist and are resolvable on the device
        val pm = activity.packageManager
        val firstIntent = buildLaunchIntent(pm, firstApp)
            ?: return LaunchResult.Failed(
                title = "${firstApp.label} Unavailable",
                reason = "Android PackageManager could not resolve a launchable Intent for ${firstApp.packageName}."
            )

        val secondIntent = buildLaunchIntent(pm, secondApp)
            ?: return LaunchResult.Failed(
                title = "${secondApp.label} Unavailable",
                reason = "Android PackageManager could not resolve a launchable Intent for ${secondApp.packageName}."
            )

        // Option A: Freeform launch ONLY when the device hardware/OS genuinely declares freeform support
        if (settings.preferFreeformWhenSupported && compatibility.supportsFreeformWindows) {
            return launchInFreeformWindows(activity, firstApp, secondApp, firstIntent, secondIntent)
        }

        // Option B: Standard Split-Screen launch
        // On Android 10-15+ phones, FLAG_ACTIVITY_LAUNCH_ADJACENT only opens an adjacent split pane
        // reliably when the caller (TwinSpace) is ALREADY in multi-window mode, or on large screens.
        // If TwinSpace is full-screen on a phone and the user has not explicitly asked to try direct
        // adjacent launch, give honest guided instructions to pin TwinSpace in Split Screen first.
        if (!activity.isInMultiWindowMode && !compatibility.isLargeScreen && !forceDirectAttempt) {
            return LaunchResult.RequiresManualSplitSetup(
                firstAppLabel = firstApp.label,
                secondAppLabel = secondApp.label,
                reason = "Android requires TwinSpace to be in Split Screen mode (or on a tablet/foldable) so FLAG_ACTIVITY_LAUNCH_ADJACENT can place ${secondApp.label} in the adjacent pane.",
                steps = listOf(
                    "Open Android Recents (swipe up and hold, or tap the Recents button).",
                    "Tap the TwinSpace icon above its preview card and select 'Split top' or 'Split screen'.",
                    "Tap any placeholder or return to TwinSpace in the active split pane.",
                    "Tap 'Open Both' again — TwinSpace will launch ${secondApp.label} in the adjacent pane and ${firstApp.label} in the primary pane."
                )
            )
        }

        return executeSplitLaunch(activity, firstApp, secondApp, firstIntent, secondIntent, settings)
    }

    private fun executeSplitLaunch(
        activity: Activity,
        firstApp: InstalledApp,
        secondApp: InstalledApp,
        firstIntent: Intent,
        secondIntent: Intent,
        settings: TwinSpaceSettings
    ): LaunchResult {
        val samePackage = firstApp.packageName == secondApp.packageName

        // Prepare second app for the adjacent window
        secondIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
        if (samePackage && settings.allowSameAppTwice) {
            secondIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        }

        // Prepare first app for TwinSpace's current pane
        firstIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        return try {
            // Launch second app into the adjacent split window first
            activity.startActivity(secondIntent)
            // Launch first app into the current pane
            activity.startActivity(firstIntent)

            val multiTaskWarning = if (!firstApp.supportsStandardMultiTask || !secondApp.supportsStandardMultiTask) {
                " Note: One of the selected apps uses singleInstance launch mode, so the system may constrain how it stacks."
            } else {
                ""
            }

            if (activity.isInMultiWindowMode) {
                LaunchResult.LaunchedSplitPair(
                    firstAppLabel = firstApp.label,
                    secondAppLabel = secondApp.label,
                    modeDescription = "Requested ${firstApp.label} and ${secondApp.label} across adjacent Android Split Screen windows.$multiTaskWarning"
                )
            } else {
                LaunchResult.PartialOrWarning(
                    title = "Adjacent Launch Requested",
                    message = "Sent FLAG_ACTIVITY_LAUNCH_ADJACENT for ${secondApp.label} and launched ${firstApp.label}. If your device opened only one app full-screen, pin TwinSpace in Split Screen from Android Recents first and retry.$multiTaskWarning"
                )
            }
        } catch (e: ActivityNotFoundException) {
            LaunchResult.Failed(
                title = "Activity Not Found",
                reason = e.localizedMessage ?: "Android could not locate the target activity."
            )
        } catch (e: SecurityException) {
            LaunchResult.Failed(
                title = "Permission Denied by Android",
                reason = e.localizedMessage ?: "Android refused to launch one of the selected apps externally."
            )
        } catch (e: Exception) {
            LaunchResult.Failed(
                title = "Multi-Window Launch Error",
                reason = e.localizedMessage ?: "An unexpected system error occurred while starting the apps."
            )
        }
    }

    private fun launchInFreeformWindows(
        activity: Activity,
        firstApp: InstalledApp,
        secondApp: InstalledApp,
        firstIntent: Intent,
        secondIntent: Intent
    ): LaunchResult {
        val bounds = getDisplayBounds(activity)
        val midX = bounds.left + bounds.width() / 2
        val leftRect = Rect(bounds.left, bounds.top, midX, bounds.bottom)
        val rightRect = Rect(midX, bounds.top, bounds.right, bounds.bottom)

        firstIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        secondIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)

        val leftOptions = ActivityOptions.makeBasic().setLaunchBounds(leftRect).toBundle()
        val rightOptions = ActivityOptions.makeBasic().setLaunchBounds(rightRect).toBundle()

        return try {
            activity.startActivity(firstIntent, leftOptions)
            activity.startActivity(secondIntent, rightOptions)
            LaunchResult.LaunchedSplitPair(
                firstAppLabel = firstApp.label,
                secondAppLabel = secondApp.label,
                modeDescription = "Launched ${firstApp.label} (left bounds) and ${secondApp.label} (right bounds) using native Freeform window bounds."
            )
        } catch (e: SecurityException) {
            LaunchResult.Failed(
                title = "Freeform Launch Refused",
                reason = e.localizedMessage ?: "System refused freeform window launch."
            )
        } catch (e: Exception) {
            LaunchResult.Failed(
                title = "Freeform Launch Failed",
                reason = e.localizedMessage ?: "Unable to open apps in freeform bounds."
            )
        }
    }

    private fun getDisplayBounds(activity: Activity): Rect {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.windowManager.currentWindowMetrics.bounds
        } else {
            val metrics = activity.resources.displayMetrics
            Rect(0, 0, metrics.widthPixels, metrics.heightPixels)
        }
    }

    private fun buildLaunchIntent(pm: PackageManager, app: InstalledApp): Intent? {
        val explicit = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = app.componentName
        }
        val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.resolveActivity(explicit, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.resolveActivity(explicit, 0)
        }
        if (resolved != null) return explicit
        return pm.getLaunchIntentForPackage(app.packageName)
    }
}
