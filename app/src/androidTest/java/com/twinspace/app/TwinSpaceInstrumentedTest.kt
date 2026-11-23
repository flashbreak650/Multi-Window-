package com.twinspace.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TwinSpaceInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun dashboard_displaysCoreControlsAndNavigatesToAllApps() {
        // Dismiss first-launch privacy disclosure if displayed
        try {
            composeRule.onNodeWithText("Continue").performClick()
        } catch (_: AssertionError) {
            // Already acknowledged
        }

        composeRule.onNodeWithText("TwinSpace").assertIsDisplayed()
        composeRule.onNodeWithText("First App").assertIsDisplayed()
        composeRule.onNodeWithText("Second App").assertIsDisplayed()
        composeRule.onNodeWithText("OPEN BOTH").assertIsDisplayed()

        composeRule.onNodeWithText("All Apps").performClick()
        composeRule.onNodeWithText("All Installed Apps").assertIsDisplayed()
    }
}
