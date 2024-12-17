package com.example.facebook.data

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import io.socket.client.IO
import okhttp3.Cookie
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import kotlin.reflect.KProperty1

class DefaultAppContainerTest {

    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var appContainer: DefaultAppContainer

    @Test
    fun `OkHttpClient should be initialized with correct timeout settings`() {
        val context = mockk<Context>()
        val sharedPreferences = mockk<SharedPreferences>()

        every {
            context.getSharedPreferences(
                "CookiePrefs",
                Context.MODE_PRIVATE
            )
        } returns sharedPreferences
        every { sharedPreferences.all } returns emptyMap()

        val appContainer = DefaultAppContainer(context)

        val okHttpClientField = appContainer.javaClass.getDeclaredField("okHttpClient")
        okHttpClientField.isAccessible = true
        val okHttpClient = okHttpClientField.get(appContainer) as OkHttpClient

        assertEquals(60, okHttpClient.readTimeoutMillis / 1000)
        assertEquals(60, okHttpClient.writeTimeoutMillis / 1000)
    }

    @Test
    fun `should create Retrofit instance with proper configuration`() {
        val context = mockk<Context>()
        val sharedPreferences = mockk<SharedPreferences>()
        every { context.getSharedPreferences(any(), any()) } returns sharedPreferences
        every { sharedPreferences.all } returns emptyMap()

        val container = DefaultAppContainer(context)
        val retrofitField = DefaultAppContainer::class.java.getDeclaredField("retrofit")
        retrofitField.isAccessible = true
        val retrofit = retrofitField.get(container) as Retrofit

        assertEquals("https://hki2425-mobdev7-nhom3.onrender.com/", retrofit.baseUrl().toString())

        val okHttpClientField = DefaultAppContainer::class.java.getDeclaredField("okHttpClient")
        okHttpClientField.isAccessible = true
        val okHttpClient = okHttpClientField.get(container) as OkHttpClient

        assertTrue(okHttpClient.cookieJar is AppCookieJar)
        assertEquals(60, okHttpClient.readTimeoutMillis / 1000)
        assertEquals(60, okHttpClient.writeTimeoutMillis / 1000)

        val converterFactories = retrofit.converterFactories()
        assertTrue(converterFactories.isNotEmpty())
        assertTrue(converterFactories.any { it::class.java.simpleName.contains("ConverterFactory") })
    }

    @Test
    fun `should lazy initialize all repository instances`() {
        val context = mockk<Context>()
        val sharedPreferences = mockk<SharedPreferences>()
        every { context.getSharedPreferences(any(), any()) } returns sharedPreferences
        every { sharedPreferences.all } returns emptyMap()

        val appContainer = DefaultAppContainer(context)

        // Access each repository to trigger lazy initialization
        appContainer.socketRepository
        appContainer.userRepository
        appContainer.messageRepository
        appContainer.chatGroupRepository
        appContainer.fileRepository
        appContainer.friendRepository
        appContainer.userPreferenceRepository

        // Verify that each repository is initialized only once
        assertTrue(isPropertyInitialized(appContainer, DefaultAppContainer::socketRepository))
        assertTrue(isPropertyInitialized(appContainer, DefaultAppContainer::userRepository))
        assertTrue(isPropertyInitialized(appContainer, DefaultAppContainer::messageRepository))
        assertTrue(isPropertyInitialized(appContainer, DefaultAppContainer::chatGroupRepository))
        assertTrue(isPropertyInitialized(appContainer, DefaultAppContainer::fileRepository))
        assertTrue(isPropertyInitialized(appContainer, DefaultAppContainer::friendRepository))
        assertTrue(
            isPropertyInitialized(
                appContainer,
                DefaultAppContainer::userPreferenceRepository
            )
        )
    }

    // Modified helper function to check if a property is initialized
    private fun <T : Any, R> isPropertyInitialized(
        instance: T,
        property: KProperty1<T, R>
    ): Boolean {
        return try {
            property.get(instance)
            true
        } catch (e: Exception) {
            false
        }
    }

    @Test
    fun `user property should return null`() {
        val context = mockk<Context>()
        val sharedPreferences = mockk<SharedPreferences>()
        every { context.getSharedPreferences(any(), any()) } returns sharedPreferences
        every { sharedPreferences.all } returns emptyMap()

        val appContainer = DefaultAppContainer(context)

        assertNull(appContainer.user)
    }

    @Test
    fun `should properly set up Socket_IO connection with extraHeaders`() {
        val mockContext = mockk<Context>()
        val mockSharedPreferences = mockk<SharedPreferences>()
        val mockEditor = mockk<SharedPreferences.Editor>()
        val mockCookie = mockk<Cookie>()
        val mockSocket = mockk<io.socket.client.Socket>()

        every { mockContext.getSharedPreferences(any(), any()) } returns mockSharedPreferences
        every { mockSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.apply() } returns Unit
        every { mockSharedPreferences.all } returns emptyMap()

        val appContainer = DefaultAppContainer(mockContext)

        mockkStatic(IO::class)
        every { IO.socket(any<String>(), any()) } returns mockSocket
        every { mockSocket.connect() } returns mockSocket
        every { mockSocket.on(any(), any()) } returns mockSocket  // Add this line

        mockkObject(Cookie.Companion)
        every { Cookie.parse(any(), any()) } returns mockCookie
        every { mockCookie.name } returns "testCookie"
        every { mockCookie.value } returns "testValue"

        val socketRepository = appContainer.socketRepository

        verify {
            IO.socket(eq("https://hki2425-mobdev7-nhom3.onrender.com/"), match {
                it.extraHeaders["Cookie"]?.first()?.startsWith("j:{") ?: false
            })
        }
        verify { mockSocket.connect() }
        verify { mockSocket.on(eq("connect"), any()) }  // Add this line
    }

    @Test
    fun `repositories should be created with appropriate types`() {
        val mockContext = mockk<Context>()
        val mockSharedPreferences = mockk<SharedPreferences>()
        every { mockContext.getSharedPreferences(any(), any()) } returns mockSharedPreferences
        every { mockSharedPreferences.all } returns emptyMap()

        val container = DefaultAppContainer(mockContext)

        assertTrue(container.userRepository is NetworkUserRepository)
        assertTrue(container.messageRepository is NetworkMessageRepository)
        assertTrue(container.chatGroupRepository is NetworkChatGroupRepository)
        assertTrue(container.fileRepository is NetworkFileRepository)
        assertTrue(container.friendRepository is NetworkFriendRepository)
        assertTrue(container.userPreferenceRepository is UserPreferenceRepository)

        // Test that the repositories are not null
        assertNotNull(container.userRepository)
        assertNotNull(container.messageRepository)
        assertNotNull(container.chatGroupRepository)
        assertNotNull(container.fileRepository)
        assertNotNull(container.friendRepository)
        assertNotNull(container.userPreferenceRepository)
    }

    @Test
    fun `should use correct base URL for all network requests`() {
        val context = mockk<Context>()
        val sharedPreferences = mockk<SharedPreferences>()
        every { context.getSharedPreferences(any(), any()) } returns sharedPreferences
        every { sharedPreferences.all } returns emptyMap()

        val appContainer = DefaultAppContainer(context)

        val expectedBaseUrl = "https://hki2425-mobdev7-nhom3.onrender.com/"

        val retrofit = appContainer.javaClass.getDeclaredField("retrofit")
        retrofit.isAccessible = true
        val retrofitInstance = retrofit.get(appContainer) as Retrofit

        assertEquals(expectedBaseUrl, retrofitInstance.baseUrl().toString())
    }
}
