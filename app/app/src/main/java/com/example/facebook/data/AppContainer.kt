package com.example.facebook.data

import android.content.Context
import android.content.SharedPreferences
import com.example.facebook.model.User
import com.example.facebook.network.ChatgroupApiService
import com.example.facebook.network.FileApiService
import com.example.facebook.network.FriendsApiService
import com.example.facebook.network.MessageApiService
import com.example.facebook.network.UserApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.socket.client.IO
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.ConcurrentHashMap

interface AppContainer {
    val userRepository: UserRepository
    val socketRepository: SocketRepository
    val fileRepository: FileRepository
    val chatGroupRepository: ChatGroupRepository
    val messageRepository: MessageRepository
    val friendRepository: FriendRepository
    val userPreferenceRepository: UserPreferenceRepository
    val user: User?
    val appCookieJar: AppCookieJar
}

class AppCookieJar(context: Context) : CookieJar {
    private val cookieStore: MutableMap<String, List<Cookie>> = ConcurrentHashMap()
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("CookiePrefs", Context.MODE_PRIVATE)

    init {
        loadCookies()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore[url.host] = cookies
        saveCookies()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url.host] ?: listOf()
    }

    private fun saveCookies() {
        val editor = sharedPreferences.edit()
        for ((key, value) in cookieStore) {
            val cookieString = value.joinToString(";") { it.toString() }
            editor.putString(key, cookieString)
        }
        editor.apply()
    }

    fun loadCookies() {
        for ((key, value) in sharedPreferences.all) {
            val cookieStrings = (value as String).split(";")
            val cookies = cookieStrings.mapNotNull { Cookie.parse("http://$key".toHttpUrl(), it) }
            cookieStore[key] = cookies
        }
    }

}

class DefaultAppContainer(context: Context) : AppContainer {
    private val baseUrl = "https://hki2425-mobdev7-nhom3.onrender.com/"
    override val appCookieJar = AppCookieJar(context)

    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(appCookieJar)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(Json {
            ignoreUnknownKeys = true
        }.asConverterFactory("application/json".toMediaType()))
        .baseUrl(baseUrl)
        .build()

    override val socketRepository: SocketRepository by lazy {
        val cookies = appCookieJar.loadForRequest(baseUrl.toHttpUrl())
        val cookieMap = cookies.associate { it.name to it.value }
        val cookieJson = Json.encodeToString(cookieMap)
        val options = IO.Options()
        options.extraHeaders = mapOf("Cookie" to listOf("j:$cookieJson"))
        val socket = IO.socket(baseUrl, options)
        socket.connect()
        SocketRepository(socket)
    }

    override val userRepository: UserRepository by lazy {
        NetworkUserRepository(retrofit.create(UserApiService::class.java))
    }

    override val messageRepository: MessageRepository by lazy {
        NetworkMessageRepository(retrofit.create(MessageApiService::class.java))
    }

    override val chatGroupRepository: ChatGroupRepository by lazy {
        NetworkChatGroupRepository(retrofit.create(ChatgroupApiService::class.java))
    }

    override val fileRepository: FileRepository by lazy {
        NetworkFileRepository(retrofit.create(FileApiService::class.java))
    }

    override val friendRepository: FriendRepository by lazy {
        NetworkFriendRepository(retrofit.create(FriendsApiService::class.java))
    }

    override val userPreferenceRepository: UserPreferenceRepository by lazy {
        UserPreferenceRepository(context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE))
    }

    override val user = null
}