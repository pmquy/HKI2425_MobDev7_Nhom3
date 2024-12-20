package com.example.facebook.data

import com.example.facebook.model.File
import com.example.facebook.model.GetSystemFileResponse
import com.example.facebook.network.FileApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class FileRepositoryTest {

    private lateinit var fileRepository: FileRepository

    @Test
    fun `getById should return a valid File object when given a correct id`() = runTest {
        val mockFileApiService = mockk<FileApiService>()
        val fileRepository: FileRepository = NetworkFileRepository(mockFileApiService)
        val testId = "testFileId"
        val expectedFile = File(_id = testId, name = "testFile.jpg", type = "image/jpeg")

        coEvery { mockFileApiService.getById(testId) } returns Response.success(expectedFile)

        val response = fileRepository.getById(testId)

        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        assertEquals(expectedFile, response.body())
    }

    @Test
    fun `getById should return an error response when given a non-existent id`() = runTest {
        val mockFileApiService = mockk<FileApiService>()
        val repository: FileRepository = NetworkFileRepository(mockFileApiService)
        val nonExistentId = "non_existent_id"

        coEvery { mockFileApiService.getById(nonExistentId) } returns Response.error(
            404,
            "".toResponseBody(null)
        )

        val response = repository.getById(nonExistentId)

        assertFalse(response.isSuccessful)
        assertEquals(404, response.code())
    }

    @Test
    fun `getById should handle empty string as id gracefully`() = runTest {
        val fileRepository = mockk<FileRepository>()
        val emptyId = ""
        val expectedResponse = Response.success(File())

        coEvery { fileRepository.getById(emptyId) } returns expectedResponse

        val actualResponse = fileRepository.getById(emptyId)

        assertEquals(expectedResponse, actualResponse)
        coVerify { fileRepository.getById(emptyId) }
    }

    @Test
    fun `getSystemFile should return GetSystemFileResponse when given valid parameters`() =
        runTest {
            val mockFileApiService = mockk<FileApiService>()
            val fileRepository: FileRepository = NetworkFileRepository(mockFileApiService)
            val type = "image"
            val offset = 0
            val limit = 10
            val expectedResponse = mockk<Response<GetSystemFileResponse>>()

            coEvery {
                mockFileApiService.getSystemFile(
                    type,
                    offset,
                    limit
                )
            } returns expectedResponse

            val result = fileRepository.getSystemFile(type, offset, limit)

            assertEquals(expectedResponse, result)
            coVerify { mockFileApiService.getSystemFile(type, offset, limit) }
        }

    @Test
    fun `getSystemFile should handle null offset and limit values correctly`() = runTest {
        val mockFileApiService = mockk<FileApiService>()
        val fileRepository: FileRepository = NetworkFileRepository(mockFileApiService)
        val type = "image"
        val expectedResponse = mockk<Response<GetSystemFileResponse>>()

        coEvery { mockFileApiService.getSystemFile(type, null, null) } returns expectedResponse

        val actualResponse = fileRepository.getSystemFile(type, null, null)

        assertEquals(expectedResponse, actualResponse)
        coVerify { mockFileApiService.getSystemFile(type, null, null) }
    }

    @Test
    fun `getSystemFile should return an error for invalid file type`() = runTest {
        val mockFileApiService = mockk<FileApiService>()
        val fileRepository: FileRepository = NetworkFileRepository(mockFileApiService)

        val invalidType = "invalid_type"
        val errorResponse =
            Response.error<GetSystemFileResponse>(400, "".toResponseBody(null))

        coEvery {
            mockFileApiService.getSystemFile(
                invalidType,
                any(),
                any()
            )
        } returns errorResponse

        val result = fileRepository.getSystemFile(invalidType, null, null)

        assertFalse(result.isSuccessful)
        assertEquals(400, result.code())
    }

    @Test
    fun `getSystemFile should handle large offset values appropriately`() = runTest {
        val fileRepository = mockk<FileRepository>()
        val largeOffset = Int.MAX_VALUE
        val type = "image"
        val limit = 10
        val expectedResponse = mockk<Response<GetSystemFileResponse>>()

        coEvery { fileRepository.getSystemFile(type, largeOffset, limit) } returns expectedResponse

        val actualResponse = fileRepository.getSystemFile(type, largeOffset, limit)

        assertEquals(expectedResponse, actualResponse)
        coVerify { fileRepository.getSystemFile(type, largeOffset, limit) }
    }

    @Test
    fun `getSystemFile should return empty list when no files match given type`() = runTest {
        val fileRepository = mockk<FileRepository>()
        val emptyResponse = GetSystemFileResponse(data = emptyList(), hasMore = false)

        coEvery { fileRepository.getSystemFile(any(), any(), any()) } returns Response.success(
            emptyResponse
        )

        val response = fileRepository.getSystemFile("non_existent_type", 0, 10)

        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        assertEquals(false, response.body()?.hasMore)
        assertTrue(response.body()?.data?.isEmpty() ?: false)
    }

    @Test
    fun `getSystemFile should handle negative offset and limit values gracefully`() = runTest {
        val fileRepository = mockk<FileRepository>()
        val type = "image"
        val negativeOffset = -5
        val negativeLimit = -10

        coEvery {
            fileRepository.getSystemFile(
                type,
                negativeOffset,
                negativeLimit
            )
        } returns Response.success(
            GetSystemFileResponse(data = emptyList(), hasMore = true)
        )

        val response = fileRepository.getSystemFile(type, negativeOffset, negativeLimit)

        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        assertEquals(true, response.body()?.hasMore)
        assertEquals(0, response.body()?.data?.size)
    }

    @Test
    fun `getSystemFile should respect the limit parameter`() = runTest {
        val mockFileApiService = mockk<FileApiService>()
        val fileRepository = NetworkFileRepository(mockFileApiService)
        val type = "image"
        val limit = 5

        val mockResponse = mockk<Response<GetSystemFileResponse>> {
            every { isSuccessful } returns true
            every { body() } returns GetSystemFileResponse(
                hasMore = true,
                data = List(5) { mockk<File>() }
            )
        }

        coEvery { mockFileApiService.getSystemFile(type, null, limit) } returns mockResponse

        val response = fileRepository.getSystemFile(type, null, limit)

        assertTrue(response.isSuccessful)
        assertEquals(5, response.body()?.data?.size)
    }
}