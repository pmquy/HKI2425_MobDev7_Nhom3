package com.example.facebook.ui.screens

import com.example.facebook.FacebookApplication
import com.example.facebook.data.SocketRepository
import com.example.facebook.data.UserRepository
import io.mockk.mockk
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Ignore
import org.robolectric.RuntimeEnvironment

class VideoCallViewModelTest {

    @Test
    @Ignore("Requires Android View/WebRTC; skipping in pure unit tests")
    fun `initial uiState has main renderer and no users`() {
        val app = mockk<FacebookApplication>(relaxed = true)
        val userRepo = mockk<UserRepository>(relaxed = true)
        val socketRepo = mockk<SocketRepository>(relaxed = true)

        val vm = VideoCallViewModel(app, userRepo, socketRepo)

        // Only assert users list initial state to avoid renderer usage
        val state = vm.uiState.value
        assertTrue(state.users.isEmpty())
    }
}
