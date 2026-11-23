package com.twinspace.app

import android.content.pm.ActivityInfo
import com.twinspace.app.data.AppDiscoveryResult
import com.twinspace.app.data.InstalledAppsRepository
import com.twinspace.app.data.TwinSpacePreferences
import com.twinspace.app.engine.DeviceCompatibilityInspector
import com.twinspace.app.engine.MultiWindowLauncher
import com.twinspace.app.model.InstalledApp
import com.twinspace.app.model.TwinSpaceSettings
import com.twinspace.app.ui.SelectionSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TwinSpaceUnitTest {

    private val sampleApps = listOf(
        InstalledApp("com.spotify.music", "com.spotify.MainActivity", "Spotify", false, ActivityInfo.LAUNCH_MULTIPLE, true),
        InstalledApp("com.android.chrome", "com.google.android.apps.chrome.Main", "Chrome", false, ActivityInfo.LAUNCH_MULTIPLE, true),
        InstalledApp("com.google.android.youtube", "com.google.android.youtube.HomeActivity", "YouTube", false, ActivityInfo.LAUNCH_SINGLE_TASK, true),
        InstalledApp("com.example.notes", "com.example.notes.MainActivity", "Notes", false, ActivityInfo.LAUNCH_SINGLE_INSTANCE, false)
    )

    @Test
    fun sortAlphabetically_ordersByLabelIgnoringCase() {
        val sorted = InstalledAppsRepository.sortAlphabetically(sampleApps)
        assertEquals(listOf("Chrome", "Notes", "Spotify", "YouTube"), sorted.map { it.label })
    }

    @Test
    fun filterApps_matchesLabelAndPackageName() {
        val byLabel = InstalledAppsRepository.filterApps(sampleApps, "spot")
        assertEquals(1, byLabel.size)
        assertEquals("Spotify", byLabel.first().label)

        val byPackage = InstalledAppsRepository.filterApps(sampleApps, "youtube")
        assertEquals(1, byPackage.size)
        assertEquals("YouTube", byPackage.first().label)
    }

    @Test
    fun selectionSlot_parsesNavigationRouteArgsSafely() {
        assertEquals(SelectionSlot.FIRST, SelectionSlot.fromRouteArg("FIRST"))
        assertEquals(SelectionSlot.SECOND, SelectionSlot.fromRouteArg("SECOND"))
        assertEquals(SelectionSlot.BROWSE_ONLY, SelectionSlot.fromRouteArg("BROWSE_ONLY"))
        assertEquals(SelectionSlot.FIRST, SelectionSlot.fromRouteArg(null))
        assertEquals(SelectionSlot.FIRST, SelectionSlot.fromRouteArg("UNKNOWN"))
    }

    @Test
    fun appDiscoveryResult_representsEmptyAndFailureMessages() {
        val empty = AppDiscoveryResult.Empty("No launchable apps found.")
        val failure = AppDiscoveryResult.Failure("Permission denied.")
        assertEquals("No launchable apps found.", empty.message)
        assertEquals("Permission denied.", failure.message)
    }

    @Test
    fun updateRecentList_deduplicatesAndPreservesNewestOrder() {
        val updated = TwinSpacePreferences.updateRecentList(
            existing = listOf("com.android.chrome", "com.spotify.music"),
            newlySelected = listOf("com.google.android.youtube", "com.android.chrome")
        )
        assertEquals(
            listOf("com.google.android.youtube", "com.android.chrome", "com.spotify.music"),
            updated
        )
    }

    @Test
    fun validateSelection_rejectsMissingAndDuplicateAppsUnlessEnabled() {
        val launcher = MultiWindowLauncher()
        val chrome = sampleApps[1]
        val spotify = sampleApps[0]

        assertNotNull(launcher.validateSelection(chrome, null, TwinSpaceSettings()))
        assertNotNull(launcher.validateSelection(chrome, chrome, TwinSpaceSettings(allowSameAppTwice = false)))
        assertNull(launcher.validateSelection(chrome, chrome, TwinSpaceSettings(allowSameAppTwice = true)))
        assertNull(launcher.validateSelection(chrome, spotify, TwinSpaceSettings()))
    }

    @Test
    fun compatibilityInspector_detectsLowRamAndAndroidVersions() {
        val android15Phone = DeviceCompatibilityInspector.evaluateCompatibility(
            sdkInt = 35,
            release = "15",
            manufacturer = "Google",
            model = "Pixel 9",
            smallestScreenWidthDp = 411,
            isLowRamDevice = false,
            isCurrentlyInMultiWindowMode = false,
            supportsFreeformFeature = false,
            isLargeScreen = false
        )
        assertTrue(android15Phone.supportsSplitScreen)
        assertFalse(android15Phone.supportsFreeformWindows)
        assertFalse(android15Phone.supportsDirectAdjacentFromFullScreen)

        val lowRamGo = DeviceCompatibilityInspector.evaluateCompatibility(
            sdkInt = 34,
            release = "14",
            manufacturer = "Generic",
            model = "Go Edition",
            smallestScreenWidthDp = 360,
            isLowRamDevice = true,
            isCurrentlyInMultiWindowMode = false,
            supportsFreeformFeature = true,
            isLargeScreen = false
        )
        assertFalse(lowRamGo.supportsSplitScreen)
        assertFalse(lowRamGo.supportsFreeformWindows)
    }
}
