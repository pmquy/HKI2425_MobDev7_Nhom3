package com.example.facebook.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.facebook.ui.components.ImagePicker
import com.example.facebook.ui.components.MediaPicker
import com.example.facebook.ui.components.MultipleImagePicker
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImagePickerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun imagePickerTest() {
        composeTestRule.setContent {
            ImagePicker(
                onImageSelected = {}
            )
        }
        composeTestRule.onNodeWithText("Chọn ảnh").assertExists().assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun multipleImagePickerTest() {
        composeTestRule.setContent {
            MultipleImagePicker(
                onImageSelected = {}
            )
        }

        composeTestRule.onNodeWithText("Chọn ảnh").assertExists().assertIsDisplayed().assertIsEnabled()
    }
}