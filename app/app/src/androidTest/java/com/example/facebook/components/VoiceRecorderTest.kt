package com.example.facebook.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.facebook.ui.components.VoiceRecorder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VoiceRecorderTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun voiceRecorderTest() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalActivityResultRegistryOwner provides FakeActivityResultRegistryOwner()
            ) {
                VoiceRecorder(
                    onDone = {}
                )
            }
        }
        composeTestRule.onNodeWithContentDescription("play").assertExists()
        composeTestRule.onNodeWithContentDescription("stop").assertExists()
        composeTestRule.onNodeWithContentDescription("pause").assertExists()
    }
}

class FakeActivityResultRegistryOwner : ActivityResultRegistryOwner {
    override val activityResultRegistry: ActivityResultRegistry = object : ActivityResultRegistry() {
        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?
        ) {
            // Simulate immediate success
            dispatchResult(requestCode, ActivityResult(Activity.RESULT_OK, null))
        }
    }
}