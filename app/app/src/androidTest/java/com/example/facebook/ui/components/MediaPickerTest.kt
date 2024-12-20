package com.example.facebook.ui.components

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.facebook.ui.components.MediaPicker
import com.example.facebook.ui.components.VoiceRecorder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaPickerTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun mediaPickerTest() {
        composeTestRule.setContent {
            MediaPicker(
                onDone = {}
            )
        }
        composeTestRule.onNodeWithText("Pick Image").assertExists().assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithText("Pick Video").assertExists().assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithText("Pick Audio").assertExists().assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithText("Pick File").assertExists().assertIsDisplayed().assertIsEnabled()
    }
}