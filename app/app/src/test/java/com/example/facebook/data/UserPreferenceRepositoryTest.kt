package com.example.facebook.data

import android.content.SharedPreferences
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserPreferenceRepositoryTest {

    private lateinit var userPreferenceRepository: UserPreferenceRepository
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    @Test
    fun setAndRetrieveValidToken() {
        val mockToken = "testToken123"
        val mockSharedPreferences = mockk<SharedPreferences>()
        val mockEditor = mockk<SharedPreferences.Editor>()
        every { mockSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.apply() } returns Unit
        every { mockSharedPreferences.getString("token", null) } returns mockToken

        val userPreferenceRepository = UserPreferenceRepository(mockSharedPreferences)

        userPreferenceRepository.setToken(mockToken)
        val retrievedToken = userPreferenceRepository.getToken()

        verify { mockSharedPreferences.edit() }
        verify { mockEditor.putString("token", mockToken) }
        verify { mockEditor.apply() }
        verify { mockSharedPreferences.getString("token", null) }
        assertEquals(mockToken, retrievedToken)
    }

    @Test
    fun `getToken should return null when no token has been set`() {
        val mockSharedPreferences = mockk<SharedPreferences>()
        every { mockSharedPreferences.getString("token", null) } returns null

        val userPreferenceRepository = UserPreferenceRepository(mockSharedPreferences)

        val result = userPreferenceRepository.getToken()

        assertNull(result)
    }

    @Test
    fun `setToken should overwrite existing token`() {
        val sharedPreferences = mockk<SharedPreferences>()
        val editor = mockk<SharedPreferences.Editor>()

        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.apply() } just Runs

        val repository = UserPreferenceRepository(sharedPreferences)

        val initialToken = "initial_token"
        val newToken = "new_token"

        repository.setToken(initialToken)
        repository.setToken(newToken)

        verify(exactly = 1) { editor.putString("token", initialToken) }
        verify(exactly = 1) { editor.putString("token", newToken) }
        verify(exactly = 2) { editor.apply() }
    }

    @Test
    fun `setToken and getToken should handle empty string as valid token`() {
        val sharedPreferences = mockk<SharedPreferences>()
        val editor = mockk<SharedPreferences.Editor>()
        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.apply() } just Runs
        every { sharedPreferences.getString("token", null) } returns ""

        val repository = UserPreferenceRepository(sharedPreferences)

        repository.setToken("")
        verify { editor.putString("token", "") }

        val result = repository.getToken()
        assertEquals("", result)
    }

    @Test
    fun `should persist token across multiple instance creations`() {
        val sharedPreferences = mockk<SharedPreferences>()
        val editor = mockk<SharedPreferences.Editor>()

        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.apply() } just Runs

        val repository1 = UserPreferenceRepository(sharedPreferences)
        val testToken = "test_token"

        repository1.setToken(testToken)

        verify { editor.putString("token", testToken) }
        verify { editor.apply() }

        every { sharedPreferences.getString("token", null) } returns testToken

        val repository2 = UserPreferenceRepository(sharedPreferences)
        val retrievedToken = repository2.getToken()

        assertEquals(testToken, retrievedToken)
    }

    @Test
    fun `setToken and getToken should handle special characters in the token`() {
        val mockSharedPreferences = mockk<SharedPreferences>()
        val mockEditor = mockk<SharedPreferences.Editor>()
        val repository = UserPreferenceRepository(mockSharedPreferences)
        val specialToken = "!@#$%^&*()_+{}[]|:;<>,.?~`"

        every { mockSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.apply() } just Runs
        every { mockSharedPreferences.getString("token", null) } returns specialToken

        repository.setToken(specialToken)
        verify { mockEditor.putString("token", specialToken) }

        val retrievedToken = repository.getToken()
        assertEquals(specialToken, retrievedToken)
    }

    @Test
    fun `getToken should return null after clearing shared preferences`() {
        val sharedPreferences = mockk<SharedPreferences>()
        val editor = mockk<SharedPreferences.Editor>()

        every { sharedPreferences.edit() } returns editor
        every { editor.clear() } returns editor
        every { editor.apply() } returns Unit
        every { sharedPreferences.getString("token", null) } returns null

        val repository = UserPreferenceRepository(sharedPreferences)

        // Clear the shared preferences
        sharedPreferences.edit().clear().apply()

        // Check if getToken returns null
        val result = repository.getToken()
        assertNull(result)
    }

    @Test
    fun `setToken and getToken should handle very long token strings`() {
        val longToken = "a".repeat(10000)
        val mockSharedPreferences = mockk<SharedPreferences>()
        val mockEditor = mockk<SharedPreferences.Editor>()
        val repository = UserPreferenceRepository(mockSharedPreferences)

        every { mockSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.apply() } returns Unit
        every { mockSharedPreferences.getString("token", null) } returns longToken

        repository.setToken(longToken)
        val retrievedToken = repository.getToken()

        assertEquals(longToken, retrievedToken)
        verify {
            mockSharedPreferences.edit()
            mockEditor.putString("token", longToken)
            mockEditor.apply()
        }
        verify { mockSharedPreferences.getString("token", null) }
    }

    @Test
    fun `setToken should not throw exception when setting null token`() {
        val sharedPreferences = mockk<SharedPreferences>()
        val editor = mockk<SharedPreferences.Editor>()
        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.apply() } returns Unit

        val repository = UserPreferenceRepository(sharedPreferences)

        // Execute the code without expecting an exception
        repository.setToken("")

        verify { editor.putString("token", "") }
        verify { editor.apply() }
    }

    @Test
    fun `setToken and getToken should maintain case sensitivity`() {
        val sharedPreferences = mockk<SharedPreferences>()
        val editor = mockk<SharedPreferences.Editor>()
        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.apply() } returns Unit
        every { sharedPreferences.getString("token", null) } returns "CaseSensitiveToken123"

        val repository = UserPreferenceRepository(sharedPreferences)

        repository.setToken("CaseSensitiveToken123")
        val retrievedToken = repository.getToken()

        verify { editor.putString("token", "CaseSensitiveToken123") }
        assertEquals("CaseSensitiveToken123", retrievedToken)
    }
}