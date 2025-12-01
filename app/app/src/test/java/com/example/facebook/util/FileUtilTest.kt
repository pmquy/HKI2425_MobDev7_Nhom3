package com.example.facebook.util

import android.content.ContentResolver
import android.database.Cursor
import android.provider.OpenableColumns
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.io.File
import io.mockk.every
import io.mockk.mockk

@RunWith(RobolectricTestRunner::class)
class FileUtilTest {

    @Test
    fun `uriToFile copies content and returns fallback metadata`() {
        val cacheDir = createTempDir(prefix = "cache")
        val context = mockk<android.content.Context>(relaxed = true)
        every { context.cacheDir } returns cacheDir

        // Mock ContentResolver
        val resolver = mockk<ContentResolver>(relaxed = true)
        every { context.contentResolver } returns resolver

        // Prepare fake cursor to return a file name
        val cursor = mockk<Cursor>(relaxed = true)
        every { cursor.moveToFirst() } returns true
        every { cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) } returns 0
        every { cursor.getString(0) } returns "source.txt"
        every { resolver.query(any(), any(), any(), any(), any()) } returns cursor

        // Prepare input stream content
        val content = "Hello World".toByteArray()
        every { resolver.openInputStream(any()) } returns ByteArrayInputStream(content)
        every { resolver.getType(any()) } returns null

        val uri = mockk<Uri>(relaxed = true)
        val (copied, mimeType) = uriToFile(context, uri)

        assertTrue(copied.exists())
        assertEquals(content.size.toLong(), copied.length())
        assertEquals("application/octet-stream", mimeType)
        assertEquals("source.txt", copied.name)
    }

    @Test
    fun `createImageFile creates jpg with prefix`() {
        val filesDir = createTempDir(prefix = "pictures")
        val context = mockk<android.content.Context>(relaxed = true)
        every { context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES) } returns filesDir
        val imageFile = createImageFile(context)
        assertTrue(imageFile.exists())
        assertTrue(imageFile.name.startsWith("JPEG_"))
        assertTrue(imageFile.name.endsWith(".jpg"))
    }

    @Test
    fun `uriToFile should throw when inputStream is null (TODO fix)`() {
        val cacheDir = createTempDir(prefix = "cache")
        val context = mockk<android.content.Context>(relaxed = true)
        every { context.cacheDir } returns cacheDir

        val resolver = mockk<ContentResolver>(relaxed = true)
        every { context.contentResolver } returns resolver

        val cursor = mockk<Cursor>(relaxed = true)
        every { cursor.moveToFirst() } returns false // triggers fallback name
        every { resolver.query(any(), any(), any(), any(), any()) } returns cursor

        // Simulate resolver failing to open stream
        every { resolver.openInputStream(any()) } returns null
        every { resolver.getType(any()) } returns null

        val uri = mockk<android.net.Uri>(relaxed = true)

        // Expected behavior: should not silently create empty file when input stream is null
        assertThrows(IllegalArgumentException::class.java) {
            uriToFile(context, uri)
        }
    }
}
