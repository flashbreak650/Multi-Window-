package com.twinspace.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
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
    fun firstAndSecondAppCards_openAppPickerAndReturnToDashboard() {
        // Dismiss first-launch privacy disclosure if displayed
        try {
            composeRule.onNodeWithText("Continue").performClick()
        } catch (_: AssertionError) {
            // Already acknowledged
        }

        composeRule.onNodeWithText("TwinSpace").assertIsDisplayed()

        // Tap First App card -> must open Select First App screen
        composeRule.onNodeWithTag("first_app_card").performClick()
        composeRule.onNodeWithText("Select First App").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Back").performClick()

        // Tap Second App card -> must open Select Second App screen
        composeRule.onNodeWithTag("second_app_card").performClick()
        composeRule.onNodeWithText("Select Second App").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Back").performClick()

        // Tap All Apps tile -> must open All Installed Apps screen
        composeRule.onNodeWithText("All Apps").performClick()
        composeRule.onNodeWithText("All Installed Apps").assertIsDisplayed()
    }
}
