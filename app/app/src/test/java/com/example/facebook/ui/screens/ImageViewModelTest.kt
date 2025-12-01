package com.example.facebook.ui.screens

import androidx.compose.ui.geometry.Offset
import com.example.facebook.FacebookApplication
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import org.robolectric.RuntimeEnvironment

class ImageViewModelTest {

    @Test
    fun `reset returns to default state`() {
        val app = mockk<FacebookApplication>(relaxed = true)
        val vm = ImageViewModel(app)

        // mutate first
        vm.onGesture(Offset(10f, 10f), Offset(5f, 5f), 2f, 0f)
        vm.reset()

        val s = vm.uiState.value
        assertEquals(1f, s.scale)
        assertEquals(1f, s.rotationState)
        assertEquals(0f, s.offsetX)
        assertEquals(0f, s.offsetY)
        assertEquals(0f, s.focalX)
        assertEquals(0f, s.focalY)
        assertEquals(false, s.isDownloading)
    }

    @Test
    fun `onGesture updates scale and offsets`() {
        val app = mockk<FacebookApplication>(relaxed = true)
        val vm = ImageViewModel(app)

        vm.onGesture(Offset(100f, 50f), Offset(10f, -5f), 1.5f, 0f)

        val s = vm.uiState.value
        assertEquals(1.5f, s.scale)
        // focal points set to centroid
        assertEquals(100f, s.focalX)
        assertEquals(50f, s.focalY)
    }
}
