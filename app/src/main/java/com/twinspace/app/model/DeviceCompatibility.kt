package com.twinspace.app.model

/**
 * Snapshot of the device's real Android multi-window capabilities and OEM context.
 */
data class DeviceCompatibility(
    val sdkInt: Int,
    val androidVersionLabel: String,
    val manufacturer: String,
    val model: String,
    val smallestScreenWidthDp: Int,
    val isLargeScreen: Boolean,
    val isLowRamDevice: Boolean,
    val isCurrentlyInMultiWindowMode: Boolean,
    val supportsSplitScreen: Boolean,
    val supportsFreeformWindows: Boolean,
    val supportsDirectAdjacentFromFullScreen: Boolean,
    val osVersionGuidance: String,
    val oemNotes: String
)
