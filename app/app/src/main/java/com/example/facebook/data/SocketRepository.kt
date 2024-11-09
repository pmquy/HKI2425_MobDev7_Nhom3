package com.example.facebook.data

import android.util.Log
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class SocketRepository(private val socket: Socket) {

    init {
        socket.on(Socket.EVENT_CONNECT) {
            Log.d("SocketService", "Connected")
        }.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Log.e("SocketService", "Connection Error: ${args[0]}")
        }.on(Socket.EVENT_DISCONNECT) {
            Log.d("SocketService", "Disconnected")
        }
    }

    fun sendMessage(event: String, vararg args: Any) {
        socket.emit(event, *args)
    }

    fun addEventListener(event: String, listener: Emitter.Listener) {
        socket.on(event, listener)
    }


    fun disconnect() {
        socket.disconnect()
    }

    fun removeEventListener(event: String) {
        socket.off(event)
    }

    fun getID(): String {
        return socket.id() ?: ""
    }

    suspend fun waitForConnection() {
        if (socket.connected()) return
        suspendCancellableCoroutine { continuation ->
            socket.once(Socket.EVENT_CONNECT) {
                continuation.resume(Unit)
            }
            socket.connect()
        }
    }
}