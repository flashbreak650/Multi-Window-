package com.twinspace.app.model

/**
 * Honest outcome returned after attempting a real Android multi-window launch.
 */
sealed class LaunchResult {
    data class LaunchedSplitPair(
        val firstAppLabel: String,
        val secondAppLabel: String,
        val modeDescription: String
    ) : LaunchResult()

    data class RequiresManualSplitSetup(
        val firstAppLabel: String,
        val secondAppLabel: String,
        val reason: String,
        val steps: List<String>
    ) : LaunchResult()

    data class PartialOrWarning(
        val title: String,
        val message: String
    ) : LaunchResult()

    data class Failed(
        val title: String,
        val reason: String
    ) : LaunchResult()
}
