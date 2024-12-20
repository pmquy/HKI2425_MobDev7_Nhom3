package com.example.facebook.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.example.facebook.data.AppCookieJar
import com.example.facebook.data.MessageRepository
import com.example.facebook.data.NetworkMessageRepository
import com.example.facebook.network.MessageApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.Locale


object RetrofitInstance {
    private var retrofit: Retrofit? = null

    private fun getInstance(context: Context): Retrofit {
        if (retrofit == null) {
            val baseUrl = "https://hki2425-mobdev7-nhom3.onrender.com/"
            val cookieJar = AppCookieJar(context)

            val okHttpClient = OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(Json {
                    ignoreUnknownKeys = true
                }.asConverterFactory("application/json".toMediaType()))
                .baseUrl(baseUrl)
                .build()
        }
        return retrofit!!
    }

    fun getMessageRepository(context: Context): MessageRepository {
        val retrofit = getInstance(context)
        return NetworkMessageRepository(retrofit.create(MessageApiService::class.java))
    }
}

object TextToSpeechInstance {
    private var textToSpeech: TextToSpeech? = null

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }

    fun speak(text: String, queueMode: Int, params: Bundle?, utteranceId: String?, context: Context) {
        if(textToSpeech == null) {
            textToSpeech = TextToSpeech(context) { status ->
                if(status == TextToSpeech.SUCCESS) {
                    textToSpeech?.setLanguage(Locale.getDefault())
                    textToSpeech?.speak(text, queueMode, params, utteranceId)
                }
            }
        } else {
            textToSpeech?.speak(text, queueMode, params, utteranceId)
        }
    }
}


class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            "REPLY" -> {
                val replyText = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(KEY_TEXT_REPLY)
                Log.d("Reply", replyText.toString())
                val chatgroup = intent.getStringExtra("chatgroup")
                val messageRepository = RetrofitInstance.getMessageRepository(context)
                if(replyText != null && chatgroup != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        messageRepository.create(
                            message = replyText.toString(),
                            chatgroup = chatgroup,
                            systemFiles = emptyList(),
                            files = emptyList(),
                        )
                    }
                }
            }
            "LIKE" -> {
                val chatgroup = intent.getStringExtra("chatgroup")
                val messageRepository = RetrofitInstance.getMessageRepository(context)
                if(chatgroup != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        messageRepository.create(
                            message = "👍",
                            chatgroup = chatgroup,
                            systemFiles = emptyList(),
                            files = emptyList(),
                        )
                    }
                }
            }
            "LISTEN" -> {
                val content = intent.getStringExtra("content")
                if(content != null) {
                    val serviceIntent = Intent(context, ListenService::class.java)
                    serviceIntent.putExtra("content", content)
                    context.startService(serviceIntent)
                }
            }
        }
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}

class ListenService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val content = intent?.getStringExtra("content")
        if (!content.isNullOrEmpty()) {
            Log.d("ListenService", "Received content: $content")
            TextToSpeechInstance.speak(
                content,
                TextToSpeech.QUEUE_FLUSH,
                null,
                null,
                this
            )
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        TextToSpeechInstance.shutdown()
    }
}
