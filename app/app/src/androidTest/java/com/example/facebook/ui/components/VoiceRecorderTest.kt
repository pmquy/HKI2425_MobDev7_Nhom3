package com.example.facebook.ui.components

import android.app.Activity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat

//@RunWith(AndroidJUnit4::class)
//class VoiceRecorderTest {
//    @get:Rule
//    val composeTestRule = createComposeRule()

//    @Test
//    fun voiceRecorderTest() {
//        composeTestRule.setContent {
//            CompositionLocalProvider(
//                LocalActivityResultRegistryOwner provides FakeActivityResultRegistryOwner()
//            ) {
//                VoiceRecorder(
//                    onDone = {}
//                )
//            }
//        }
//        composeTestRule.onNodeWithContentDescription("play").assertExists()
//        composeTestRule.onNodeWithContentDescription("stop").assertExists()
//        composeTestRule.onNodeWithContentDescription("pause").assertExists()
//    }
//}

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