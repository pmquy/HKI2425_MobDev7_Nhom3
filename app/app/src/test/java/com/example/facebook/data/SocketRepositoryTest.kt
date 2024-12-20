package com.example.facebook.data

import android.util.Log
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SocketRepositoryTest {

    @MockK
    private lateinit var mockSocket: Socket

    private lateinit var socketRepository: SocketRepository

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        mockSocket = mockk<Socket>()
        MockKAnnotations.init(this)
        every { mockSocket.on(any(), any()) } returns mockSocket
        socketRepository = SocketRepository(mockSocket)

    }


    @Test
    fun `should successfully connect to the socket and log the connection`() = runTest {
        val mockSocket = mockk<Socket>()
        val logSlot = slot<String>()
        val listenerSlots = mutableListOf<Emitter.Listener>()

        every { mockSocket.on(any(), capture(listenerSlots)) } returns mockSocket

        mockkStatic(Log::class)
        every { Log.d(any(), capture(logSlot)) } returns 0

        val socketRepository = SocketRepository(mockSocket)

        // Verify that the socket.on() method was called for all three events
        verify(exactly = 1) { mockSocket.on(Socket.EVENT_CONNECT, any()) }
        verify(exactly = 1) { mockSocket.on(Socket.EVENT_CONNECT_ERROR, any()) }
        verify(exactly = 1) { mockSocket.on(Socket.EVENT_DISCONNECT, any()) }

        // Find the EVENT_CONNECT listener
        val connectListener = listenerSlots.find {
            mockSocket.on(Socket.EVENT_CONNECT, it) == mockSocket
        }

        // Simulate the connection event
        connectListener?.call()

        verify {
            Log.d("SocketService", "Connected")
        }

        assertEquals("Connected", logSlot.captured)
    }

    @Test
    fun `should handle and log connection errors appropriately`() {
        val mockArgs = arrayOf<Any>("Test error message")

        // Mock all Socket.on calls to return the mockSocket
        every { mockSocket.on(any(), any()) } returns mockSocket

        val socketRepository = SocketRepository(mockSocket)

        // Capture the connection error listener
        val listenerSlot = slot<Emitter.Listener>()
        every {
            mockSocket.on(
                Socket.EVENT_CONNECT_ERROR,
                capture(listenerSlot)
            )
        } returns mockSocket

        // Create the SocketRepository instance, which will set up the listeners
        SocketRepository(mockSocket)

        // Trigger the connection error event
        listenerSlot.captured.call(mockArgs)

        // Verify that the error is logged appropriately
        verify {
            Log.e(
                "SocketService",
                match { it.startsWith("Connection Error:") }
            )
        }
    }

    @Test
    fun `should log disconnection events correctly`() {
        val mockSocket = mockk<Socket>()

        every { mockSocket.on(Socket.EVENT_CONNECT, any()) } returns mockSocket
        every { mockSocket.on(Socket.EVENT_CONNECT_ERROR, any()) } returns mockSocket
        every { mockSocket.on(Socket.EVENT_DISCONNECT, any()) } returns mockSocket

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        SocketRepository(mockSocket)

        val disconnectListeners = mutableListOf<Emitter.Listener>()
        verify(exactly = 1) {
            mockSocket.on(Socket.EVENT_DISCONNECT, capture(disconnectListeners))
        }

        disconnectListeners.forEach { it.call(emptyArray<Any>()) }

        verify(exactly = 1) { Log.d("SocketService", "Disconnected") }
    }

    @Test
    fun `sendMessage should emit event with correct arguments`() {
        val mockSocket = mockk<Socket>()
        every { mockSocket.on(any(), any()) } returns mockSocket
        every { mockSocket.emit(any(), *anyVararg()) } returns mockSocket

        val socketRepository = SocketRepository(mockSocket)
        val event = "testEvent"
        val arg1 = "arg1"
        val arg2 = 42

        socketRepository.sendMessage(event, arg1, arg2)

        verify { mockSocket.emit(event, arg1, arg2) }
    }

    @Test
    fun `addEventListener should add listener for custom event`() {
        val mockSocket = mockk<Socket>()
        every { mockSocket.on(any(), any()) } returns mockSocket
        every { mockSocket.on(Socket.EVENT_CONNECT, any()) } returns mockSocket
        every { mockSocket.on(Socket.EVENT_CONNECT_ERROR, any()) } returns mockSocket
        every { mockSocket.on(Socket.EVENT_DISCONNECT, any()) } returns mockSocket

        val socketRepository = SocketRepository(mockSocket)
        val eventName = "customEvent"
        val listener = mockk<Emitter.Listener>()

        every { mockSocket.on(eventName, listener) } returns mockSocket

        socketRepository.addEventListener(eventName, listener)

        verify { mockSocket.on(eventName, listener) }
    }

    @Test
    fun `removeEventListener should remove specific event listener`() {
        val mockSocket = mockk<Socket>()

        // Mock the Socket.on() method calls in the constructor
        every { mockSocket.on(Socket.EVENT_CONNECT, any()) } returns mockSocket
        every { mockSocket.on(Socket.EVENT_CONNECT_ERROR, any()) } returns mockSocket
        every { mockSocket.on(Socket.EVENT_DISCONNECT, any()) } returns mockSocket

        val socketRepository = SocketRepository(mockSocket)
        val eventName = "testEvent"

        every { mockSocket.off(eventName) } returns mockSocket

        socketRepository.removeEventListener(eventName)

        verify(exactly = 1) { mockSocket.off(eventName) }
    }

    @Test
    fun `getID should return correct socket ID when connected`() {
        val mockSocket = mockk<Socket>()
        every { mockSocket.id() } returns "test-socket-id"
        every { mockSocket.on(any(), any()) } returns mockSocket
        every { mockSocket.on(Socket.EVENT_CONNECT, any()) } returns mockSocket
        every { mockSocket.on(Socket.EVENT_CONNECT_ERROR, any()) } returns mockSocket
        every { mockSocket.on(Socket.EVENT_DISCONNECT, any()) } returns mockSocket

        val socketRepository = SocketRepository(mockSocket)

        val result = socketRepository.getID()

        assertEquals("test-socket-id", result)
    }

    @Test
    fun `getID should return empty string when socket is not connected`() {
        val mockSocket = mockk<Socket>()
        every { mockSocket.id() } returns null
        every { mockSocket.connected() } returns false
        every { mockSocket.on(any(), any()) } returns mockSocket
        every { mockSocket.off(any()) } returns mockSocket

        val socketRepository = SocketRepository(mockSocket)

        assertEquals("", socketRepository.getID())
    }

    @Test
    fun `waitForConnection should wait for connection when socket is not already connected`() =
        runTest {
            every { mockSocket.connected() } returns false
            every { mockSocket.once(Socket.EVENT_CONNECT, any()) } answers {
                val listener = secondArg<Emitter.Listener>()
                listener.call()
                mockk<Emitter>() // Return a mocked Emitter object
            }
            every { mockSocket.connect() } returns mockk()

            socketRepository.waitForConnection()

            verify(exactly = 1) {
                mockSocket.once(Socket.EVENT_CONNECT, any())
                mockSocket.connect()
            }
        }

    @Test
    fun `waitForConnection should immediately return if socket is already connected`() = runTest {
        val mockSocket = mockk<Socket>()
        every { mockSocket.connected() } returns true
        every { mockSocket.on(any(), any()) } returns mockSocket
        every { mockSocket.id() } returns "mockId"

        val socketRepository = SocketRepository(mockSocket)

        socketRepository.waitForConnection()

        verify(exactly = 1) { mockSocket.connected() }
        verify(exactly = 0) { mockSocket.once(any(), any()) }
        verify(exactly = 0) { mockSocket.connect() }
        verify(exactly = 3) { mockSocket.on(any(), any()) }
    }
}

