package com.example.facebook.data

import android.content.Context
import android.content.SharedPreferences
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppCookieJarTest {

    private lateinit var appCookieJar: AppCookieJar
    private val mockContext: Context = mockk()
    private val mockSharedPreferences: SharedPreferences = mockk()
    private val mockEditor: SharedPreferences.Editor = mockk(relaxed = true)

    @Test
    fun `saveFromResponse should save cookies for given URL`() {
        val context = mockk<Context>()
        val sharedPreferences = mockk<SharedPreferences>()
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)

        every {
            context.getSharedPreferences(
                "CookiePrefs",
                Context.MODE_PRIVATE
            )
        } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { sharedPreferences.all } returns emptyMap()

        val appCookieJar = AppCookieJar(context)

        val url = "https://example.com".toHttpUrl()
        val cookies = listOf(
            Cookie.Builder().name("cookie1").value("value1").domain("example.com").build(),
            Cookie.Builder().name("cookie2").value("value2").domain("example.com").build()
        )

        appCookieJar.saveFromResponse(url, cookies)

        verify {
            editor.putString("example.com", any())
            editor.apply()
        }

        val savedCookies = appCookieJar.loadForRequest(url)
        assertEquals(2, savedCookies.size)
        assertEquals("cookie1", savedCookies[0].name)
        assertEquals("cookie2", savedCookies[1].name)
    }

    @Test
    fun `loadForRequest should return cookies for a specific URL`() {
        val context = mockk<Context>()
        val sharedPreferences = mockk<SharedPreferences>()
        every {
            context.getSharedPreferences(
                "CookiePrefs",
                Context.MODE_PRIVATE
            )
        } returns sharedPreferences
        every { sharedPreferences.all } returns mapOf("example.com" to "name=value; domain=example.com; name2=value2")

        val appCookieJar = AppCookieJar(context)
        val url = "https://example.com".toHttpUrl()
        val cookies = appCookieJar.loadForRequest(url)

        assertEquals(3, cookies.size)
        assertEquals("name", cookies[0].name)
        assertEquals("value", cookies[0].value)
        assertEquals("example.com", cookies[0].domain)
        assertEquals("domain", cookies[1].name)
        assertEquals("example.com", cookies[1].value)
        assertEquals("example.com", cookies[1].domain)
        assertEquals("name2", cookies[2].name)
        assertEquals("value2", cookies[2].value)
        assertEquals("example.com", cookies[2].domain)
    }

    @Test
    fun `loadForRequest should return empty list when no cookies are found for URL`() {
        val context = mockk<Context>()
        val sharedPreferences = mockk<SharedPreferences>()
        every {
            context.getSharedPreferences(
                "CookiePrefs",
                Context.MODE_PRIVATE
            )
        } returns sharedPreferences
        every { sharedPreferences.all } returns emptyMap()

        val appCookieJar = AppCookieJar(context)
        val url = "https://example.com".toHttpUrl()

        val result = appCookieJar.loadForRequest(url)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `saveCookies should correctly save cookies to SharedPreferences`() {
        val context = mockk<Context>()
        val sharedPreferences = mockk<SharedPreferences>()
        val editor = mockk<SharedPreferences.Editor>()

        every {
            context.getSharedPreferences(
                "CookiePrefs",
                Context.MODE_PRIVATE
            )
        } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.apply() } just Runs
        every { sharedPreferences.all } returns emptyMap()

        val appCookieJar = AppCookieJar(context)
        val url = "https://example.com".toHttpUrl()
        val cookies = listOf(
            Cookie.Builder().name("cookie1").value("value1").domain("example.com").path("/")
                .build(),
            Cookie.Builder().name("cookie2").value("value2").domain("example.com").path("/").build()
        )

        appCookieJar.saveFromResponse(url, cookies)

        verify {
            editor.putString(
                "example.com",
                "cookie1=value1; domain=example.com; path=/;cookie2=value2; domain=example.com; path=/"
            )
            editor.apply()
        }
    }

    @Test
    fun `should load cookies from SharedPreferences on initialization`() {
        val mockContext = mockk<Context>()
        val mockSharedPreferences = mockk<SharedPreferences>()
        val mockSharedPreferencesEditor = mockk<SharedPreferences.Editor>()

        every {
            mockContext.getSharedPreferences(
                "CookiePrefs",
                Context.MODE_PRIVATE
            )
        } returns mockSharedPreferences
        every { mockSharedPreferences.all } returns mapOf(
            "example.com" to "name1=value1; name2=value2",
            "test.com" to "name3=value3"
        )

        val appCookieJar = AppCookieJar(mockContext)

        val exampleCookies = appCookieJar.loadForRequest("http://example.com".toHttpUrl())
        val testCookies = appCookieJar.loadForRequest("http://test.com".toHttpUrl())

        assertEquals(2, exampleCookies.size)
        assertEquals("name1", exampleCookies[0].name)
        assertEquals("value1", exampleCookies[0].value)
        assertEquals("name2", exampleCookies[1].name)
        assertEquals("value2", exampleCookies[1].value)

        assertEquals(1, testCookies.size)
        assertEquals("name3", testCookies[0].name)
        assertEquals("value3", testCookies[0].value)
    }

    @Test
    fun `should handle multiple cookies for the same URL`() {
        val context = mockk<Context>()
        val sharedPreferences = mockk<SharedPreferences>()
        val editor = mockk<SharedPreferences.Editor>()

        every {
            context.getSharedPreferences(
                "CookiePrefs",
                Context.MODE_PRIVATE
            )
        } returns sharedPreferences
        every { sharedPreferences.all } returns emptyMap()
        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.apply() } just Runs

        val appCookieJar = AppCookieJar(context)
        val url = "https://example.com".toHttpUrl()
        val cookies = listOf(
            Cookie.Builder().name("cookie1").value("value1").domain("example.com").build(),
            Cookie.Builder().name("cookie2").value("value2").domain("example.com").build()
        )

        appCookieJar.saveFromResponse(url, cookies)

        val loadedCookies = appCookieJar.loadForRequest(url)
        assertEquals(2, loadedCookies.size)
        assertEquals("cookie1", loadedCookies[0].name)
        assertEquals("value1", loadedCookies[0].value)
        assertEquals("cookie2", loadedCookies[1].name)
        assertEquals("value2", loadedCookies[1].value)

        verify { editor.putString("example.com", any()) }
        verify { editor.apply() }
    }

    @Test
    fun `AppCookieJar should correctly parse cookie strings from SharedPreferences`() {
        val context = mockk<Context>()
        val sharedPreferences = mockk<SharedPreferences>()

        every {
            context.getSharedPreferences(
                "CookiePrefs",
                Context.MODE_PRIVATE
            )
        } returns sharedPreferences
        every { sharedPreferences.all } returns mapOf(
            "example.com" to "name1=value1; name2=value2",
            "test.com" to "name3=value3"
        )

        val cookieJar = AppCookieJar(context)

        val exampleCookies = cookieJar.loadForRequest("http://example.com".toHttpUrl())
        val testCookies = cookieJar.loadForRequest("http://test.com".toHttpUrl())

        assertEquals(2, exampleCookies.size)
        assertEquals("name1", exampleCookies[0].name)
        assertEquals("value1", exampleCookies[0].value)
        assertEquals("name2", exampleCookies[1].name)
        assertEquals("value2", exampleCookies[1].value)

        assertEquals(1, testCookies.size)
        assertEquals("name3", testCookies[0].name)
        assertEquals("value3", testCookies[0].value)
    }

    @Test
    fun `saveFromResponse should overwrite existing cookies for a URL`() {
        val context = mockk<Context>()
        val sharedPreferences = mockk<SharedPreferences>()
        val editor = mockk<SharedPreferences.Editor>()

        every {
            context.getSharedPreferences(
                "CookiePrefs",
                Context.MODE_PRIVATE
            )
        } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.apply() } just Runs
        every { sharedPreferences.all } returns emptyMap()

        val appCookieJar = AppCookieJar(context)
        val url = "https://example.com".toHttpUrl()
        val initialCookies = listOf(
            Cookie.Builder().name("cookie1").value("value1").domain("example.com").build()
        )
        val newCookies = listOf(
            Cookie.Builder().name("cookie2").value("value2").domain("example.com").build()
        )

        appCookieJar.saveFromResponse(url, initialCookies)
        appCookieJar.saveFromResponse(url, newCookies)

        val loadedCookies = appCookieJar.loadForRequest(url)

        assertEquals(newCookies, loadedCookies)
        verify(exactly = 2) { editor.putString("example.com", any()) }
    }

    @Test
    fun `AppCookieJar should handle URLs with different hosts separately`() {
        val context = mockk<Context>()
        val sharedPreferences = mockk<SharedPreferences>()
        val editor = mockk<SharedPreferences.Editor>()

        every {
            context.getSharedPreferences(
                "CookiePrefs",
                Context.MODE_PRIVATE
            )
        } returns sharedPreferences
        every { sharedPreferences.all } returns emptyMap()
        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.apply() } just Runs

        val appCookieJar = AppCookieJar(context)

        val url1 = "https://example.com".toHttpUrl()
        val url2 = "https://another-example.com".toHttpUrl()

        val cookies1 = listOf(mockk<Cookie>())
        val cookies2 = listOf(mockk<Cookie>())

        appCookieJar.saveFromResponse(url1, cookies1)
        appCookieJar.saveFromResponse(url2, cookies2)

        assertEquals(cookies1, appCookieJar.loadForRequest(url1))
        assertEquals(cookies2, appCookieJar.loadForRequest(url2))
        assertNotEquals(cookies1, appCookieJar.loadForRequest(url2))
        assertNotEquals(cookies2, appCookieJar.loadForRequest(url1))

        verify { editor.putString(any(), any()) }
        verify { editor.apply() }
    }

    @Test
    fun `AppCookieJar should maintain thread-safety when accessing the cookie store`() =
        runBlocking {
            val context = mockk<Context>()
            val sharedPreferences = mockk<SharedPreferences>()
            val editor = mockk<SharedPreferences.Editor>()

            every {
                context.getSharedPreferences(
                    "CookiePrefs",
                    Context.MODE_PRIVATE
                )
            } returns sharedPreferences
            every { sharedPreferences.all } returns emptyMap()
            every { sharedPreferences.edit() } returns editor
            every { editor.putString(any(), any()) } returns editor
            every { editor.apply() } just Runs

            val appCookieJar = AppCookieJar(context)
            val url1 = "https://example1.com".toHttpUrl()
            val url2 = "https://example2.com".toHttpUrl()
            val cookies1 = listOf(mockk<Cookie>())
            val cookies2 = listOf(mockk<Cookie>())

            val job1 = launch { appCookieJar.saveFromResponse(url1, cookies1) }
            val job2 = launch { appCookieJar.saveFromResponse(url2, cookies2) }

            job1.join()
            job2.join()

            assertEquals(cookies1, appCookieJar.loadForRequest(url1))
            assertEquals(cookies2, appCookieJar.loadForRequest(url2))
        }
}
