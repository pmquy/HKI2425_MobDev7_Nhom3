package com.example.facebook.components

import android.net.Uri
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.facebook.ui.components.AudioFile
import com.example.facebook.ui.components.FileViewModel
import com.example.facebook.ui.components.fakeFileRepository
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    private lateinit var fileViewModel: FileViewModel

    @Before
    fun setUp() {
        val fakeFileRepository = fakeFileRepository()

        fileViewModel = FileViewModel(
            fileRepository = fakeFileRepository,
            socketRepository = mockk(relaxed = true),
        )
    }

//    @Test
//    fun thisFileTest() {
//        val allowOrigin = true
//        composeTestRule.setContent {
//            File(
//                id = "1",
//                fileViewModel = fileViewModel,
//                allowOrigin = allowOrigin
//            )
//        }
//        composeTestRule.onNodeWithTag("BoxFile").assertExists()
//        val thisFile = fileViewModel.getFileById("1")
//        when (thisFile.value?.type) {
//            "image" -> {
//                when (thisFile.value?.status) {
//                    "safe" -> composeTestRule.onNodeWithTag("ImageFile").assertExists()
//                    "unsafe" -> {
//                        composeTestRule.onNodeWithTag("UnsafeFile").assertExists()
//                        if (allowOrigin) {
//                            composeTestRule.onNodeWithText("View anyway").assertExists().assertIsDisplayed()
//                        }
//                    }
//                    "processing" -> composeTestRule.onNodeWithTag("ProcessingFile").assertExists()
//                }
//            }
//            "video" -> {
//                composeTestRule.onNodeWithTag("VideoFile").assertExists()
//            }
//            "audio" -> {
//                composeTestRule.onNodeWithTag("AudioFile").assertExists()
//            }
//            else -> {
//                composeTestRule.onNodeWithText("Unknown file type").assertExists().assertIsDisplayed()
//            }
//        }
//    }

    @Test
    fun audioFileTest() {
        var playing = false
        composeTestRule.setContent {
            AudioFile(
                uri = Uri.parse(fileViewModel.getFileById("1").value?.url ?: ""),
                description = fileViewModel.getFileById("1").value?.description ?: ""
            )
        }
        composeTestRule.onNodeWithContentDescription("Mic").assertExists()
        if(playing) {
            composeTestRule.onNodeWithContentDescription("Pause").assertExists()
        } else {
            composeTestRule.onNodeWithContentDescription("PlayArrow").assertExists()
            composeTestRule.onNodeWithContentDescription("PlayArrow").assertExists().performClick()
            playing = true
        }
    }
}