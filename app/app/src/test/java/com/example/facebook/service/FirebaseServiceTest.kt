package com.example.facebook.service

import android.app.NotificationManager
import android.content.Context
import android.location.LocationManager
import androidx.media3.common.util.Log
import com.google.firebase.messaging.RemoteMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class FirebaseServiceTest {

    private lateinit var firebaseService: FirebaseService
    private lateinit var mockContext: Context
    private lateinit var mockNotificationManager: NotificationManager
    private lateinit var mockLocationManager: LocationManager

    @Before
    fun setup() {
        mockContext = mockk<Context>(relaxed = true)
        mockNotificationManager = mockk<NotificationManager>(relaxed = true)
        mockLocationManager = mockk<LocationManager>(relaxed = true)

        every { mockContext.getSystemService(Context.NOTIFICATION_SERVICE) } returns mockNotificationManager
        every { mockContext.getSystemService(Context.LOCATION_SERVICE) } returns mockLocationManager

        firebaseService = spyk(FirebaseService())
        every { firebaseService.applicationContext } returns mockContext

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns Unit
    }

    @Test
    fun `should log unknown message type when receiving a message with an unrecognized type`() {
        val mockRemoteMessage = mockk<RemoteMessage>()
        val mockData = mapOf("type" to "unknown_type")
        every { mockRemoteMessage.data } returns mockData

        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0

        val firebaseService = FirebaseService()
        firebaseService.onMessageReceived(mockRemoteMessage)

        verify { android.util.Log.d("FirebaseService", "Unknown message type: unknown_type") }
    }
}