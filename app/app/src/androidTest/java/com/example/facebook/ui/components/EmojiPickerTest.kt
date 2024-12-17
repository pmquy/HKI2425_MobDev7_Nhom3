package com.example.facebook.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.facebook.ui.components.EmojiPicker
import com.example.facebook.ui.components.GifPicker
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmojiPickerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emojiPickerTest() {
        composeTestRule.setContent {
            EmojiPicker(
                onClick = {}
            )
        }
        composeTestRule.onNodeWithTag("EmojiPicker").assertExists()
    }
}