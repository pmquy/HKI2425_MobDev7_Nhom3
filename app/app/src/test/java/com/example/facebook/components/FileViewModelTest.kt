package com.example.facebook.components

import com.example.facebook.data.FileRepository
import com.example.facebook.data.SocketRepository
import com.example.facebook.model.File
import com.example.facebook.model.GetSystemFileResponse
import com.example.facebook.ui.components.FileViewModel
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class FileViewModelTest {

    private lateinit var viewModel: FileViewModel
    private val fileRepository = mockk<FileRepository>()
    private val socketRepository = mockk<SocketRepository>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FileViewModel(fileRepository, socketRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getFileById returns null when file does not exist`() = runTest {
        val response = Response.success<File?>(null)
        coEvery { fileRepository.getById("nonexistent_id") } returns response

        val result = viewModel.getFileById("nonexistent_id").first()
        assertNull(result)
    }

    @Test
    fun `getSystemFile retrieves files from repository`() = runTest {
        val mockFiles = listOf(
            File(_id = "file1", url = "http://example.com/file1", type = "image"),
            File(_id = "file2", url = "http://example.com/file2", type = "video")
        )
        val response = GetSystemFileResponse(hasMore = false, data = mockFiles)
        coEvery { fileRepository.getSystemFile(any(), any(), any()) } returns Response.success(
            response
        )

        val result = viewModel.getSystemFile("image", 0, 10)

        assertEquals(2, result.size)
        assertEquals("http://example.com/file1", result[0].url)
        assertEquals("http://example.com/file2", result[1].url)
    }

    @Test
    fun `getSystemFile caches files`() = runTest {
        val mockFiles = listOf(
            File(_id = "file1", url = "http://example.com/file1", type = "image"),
            File(_id = "file2", url = "http://example.com/file2", type = "video")
        )
        val response = GetSystemFileResponse(hasMore = true, data = mockFiles)
        coEvery { fileRepository.getSystemFile(any(), any(), any()) } returns Response.success(
            response
        )

        val result = viewModel.getSystemFile("image", 0, 10)
        assertEquals(2, result.size)

        // Verify caching behavior
        val cachedFile = viewModel.getFileById("file1").first()
        assertNotNull(cachedFile)
        assertEquals("http://example.com/file1", cachedFile?.url)
    }

    @Test
    fun `init adds socket event listener for file_update`() {
        verify { socketRepository.addEventListener("file_update", any()) }
    }

    @Test
    fun `retrieve non-existing file returns null`() = runTest {
        coEvery { fileRepository.getById("invalid_id") } returns Response.success(null)

        val result = viewModel.getFileById("invalid_id").first()
        assertNull(result)
    }

    @Test
    fun `retrieving system files populates cache`() = runTest {
        val mockFiles = listOf(
            File(_id = "file1", url = "http://example.com/file1", type = "image"),
            File(_id = "file2", url = "http://example.com/file2", type = "audio")
        )
        val response = Response.success(GetSystemFileResponse(hasMore = false, data = mockFiles))
        coEvery { fileRepository.getSystemFile(any(), any(), any()) } returns response

        val retrievedFiles = viewModel.getSystemFile("image", offset = 0, limit = 10)
        assertEquals(mockFiles.size, retrievedFiles.size)

        val firstFileFromCache = viewModel.getFileById("file1").first()
        assertEquals(mockFiles.first(), firstFileFromCache)
    }

    // This function simulates the JSON object emission from socket for testing purposes
    private fun simulateSocketEmission(event: String, id: String, update: String) {
        socketRepository.addEventListener(event) { callbackArgs ->
            val idArg = callbackArgs.getOrNull(0) as? String
            val jsonArg = callbackArgs.getOrNull(1) as? JSONObject

            if (idArg == id) {
                val updatedFile = jsonArg?.let {
                    File(
                        _id = id,
                        url = it.optString("url"),
                        status = it.optString("status"),
                        description = it.optString("description"),
                    )
                }
                if (updatedFile != null) {
                    viewModel.getFileById(id).update { updatedFile }
                }
            }
        }
    }
}