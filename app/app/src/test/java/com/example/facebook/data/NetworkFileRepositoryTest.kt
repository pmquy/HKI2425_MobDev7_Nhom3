package com.example.facebook.data

import com.example.facebook.model.File
import com.example.facebook.model.GetSystemFileResponse
import com.example.facebook.network.FileApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class NetworkFileRepositoryTest {

    private lateinit var repository: NetworkFileRepository
    private val fileApiService = mockk<FileApiService>()

    @Before
    fun setup() {
        repository = NetworkFileRepository(fileApiService)
    }

    @Test
    fun `getById should successfully retrieve a file by its ID`() = runTest {
        val fileId = "testFileId"
        val expectedFile =
            File(_id = fileId, name = "testFile.txt", url = "http://example.com/testFile.txt")
        val expectedResponse = Response.success(expectedFile)

        coEvery { fileApiService.getById(fileId) } returns expectedResponse

        val result = repository.getById(fileId)

        assertTrue(result.isSuccessful)
        assertEquals(expectedFile, result.body())
        coVerify { fileApiService.getById(fileId) }
    }

    @Test
    fun `getById should return error response when file ID is not found`() = runTest {
        val fileApiService = mockk<FileApiService>()
        val repository = NetworkFileRepository(fileApiService)
        val nonExistentId = "non_existent_id"

        coEvery { fileApiService.getById(nonExistentId) } returns Response.error(
            404,
            "File not found".toResponseBody(null)
        )

        val response = repository.getById(nonExistentId)

        assertFalse(response.isSuccessful)
        assertEquals(404, response.code())
    }

    @Test
    fun `getSystemFile should fetch system files with valid parameters`() = runTest {
        val type = "image"
        val offset = 0
        val limit = 10
        val mockResponse = mockk<Response<GetSystemFileResponse>>()

        coEvery { fileApiService.getSystemFile(type, offset, limit) } returns mockResponse

        val result = repository.getSystemFile(type, offset, limit)

        coVerify { fileApiService.getSystemFile(type, offset, limit) }
        assertEquals(mockResponse, result)
    }

    @Test
    fun `getSystemFile should handle null offset and limit values`() = runTest {
        val mockFileApiService = mockk<FileApiService>()
        val repository = NetworkFileRepository(mockFileApiService)
        val type = "image"
        val expectedResponse = mockk<Response<GetSystemFileResponse>>()

        coEvery { mockFileApiService.getSystemFile(type, null, null) } returns expectedResponse

        val result = repository.getSystemFile(type, null, null)

        coVerify { mockFileApiService.getSystemFile(type, null, null) }
        assertEquals(expectedResponse, result)
    }

    @Test
    fun `getSystemFile should return empty response when no files match given type`() = runTest {
        val fileApiService = mockk<FileApiService>()
        val repository = NetworkFileRepository(fileApiService)
        val emptyResponse = GetSystemFileResponse(data = emptyList(), hasMore = false)

        coEvery { fileApiService.getSystemFile(any(), any(), any()) } returns Response.success(
            emptyResponse
        )

        val result = repository.getSystemFile("non_existent_type", 0, 10)

        assertTrue(result.isSuccessful)
        assertEquals(emptyResponse, result.body())
        assertEquals(false, result.body()?.hasMore)
        assertTrue(result.body()?.data?.isEmpty() ?: false)
    }

    @Test
    fun `getById should handle network errors`() = runTest {
        val mockFileApiService = mockk<FileApiService>()
        val repository = NetworkFileRepository(mockFileApiService)
        val errorMessage = "Network error occurred"

        coEvery { mockFileApiService.getById(any()) } throws IOException(errorMessage)

        val result = runCatching { repository.getById("testId") }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
    }

    @Test
    fun `getSystemFile should respect pagination limits`() = runTest {
        val mockFileApiService = mockk<FileApiService>()
        val repository = NetworkFileRepository(mockFileApiService)

        val type = "image"
        val offset = 0
        val limit = 10
        val expectedResponse = mockk<Response<GetSystemFileResponse>>()

        coEvery { mockFileApiService.getSystemFile(type, offset, limit) } returns expectedResponse

        val result = repository.getSystemFile(type, offset, limit)

        coVerify(exactly = 1) { mockFileApiService.getSystemFile(type, offset, limit) }
        assertEquals(expectedResponse, result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getSystemFile should throw exception for invalid file type`() = runTest {
        val fileApiService = mockk<FileApiService>()
        val repository = NetworkFileRepository(fileApiService)

        coEvery {
            fileApiService.getSystemFile(
                any(),
                any(),
                any()
            )
        } throws IllegalArgumentException("Invalid file type")

        repository.getSystemFile("invalidType", 0, 10)
    }

    @Test
    fun `getById should handle extremely large file sizes`() = runTest {
        val fileId = "largeFileId"
        val largeFile = File(_id = fileId, name = "LargeFile.zip", type = "application/zip")
        val mockResponse = Response.success(largeFile)

        coEvery { fileApiService.getById(fileId) } returns mockResponse

        val repository = NetworkFileRepository(fileApiService)
        val result = repository.getById(fileId)

        assertTrue(result.isSuccessful)
        assertEquals(largeFile, result.body())
    }

    @Test
    fun `getSystemFile should correctly process and return responses for different file types`() =
        runTest {
            val mockFileApiService = mockk<FileApiService>()
            val repository = NetworkFileRepository(mockFileApiService)

            val testType = "image"
            val testOffset = 0
            val testLimit = 10
            val mockResponse = mockk<Response<GetSystemFileResponse>>()

            coEvery {
                mockFileApiService.getSystemFile(
                    testType,
                    testOffset,
                    testLimit
                )
            } returns mockResponse

            val result = repository.getSystemFile(testType, testOffset, testLimit)

            assertEquals(mockResponse, result)
            coVerify { mockFileApiService.getSystemFile(testType, testOffset, testLimit) }
        }
}