package com.example.facebook.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.facebook.ui.components.GifPicker
import com.example.facebook.ui.components.ImagePicker
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GifPickerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gifPickerTest() {
        composeTestRule.setContent {
            GifPicker(
                onClick = {}
            )
        }
        composeTestRule.onNodeWithTag("GifPicker").assertExists()
    }
}