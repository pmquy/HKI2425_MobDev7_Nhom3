package com.example.facebook.data

import com.example.facebook.model.Message
import com.example.facebook.network.MessageApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.File

class NetworkMessageRepositoryTest {

    private lateinit var repository: NetworkMessageRepository
    private val messageApiService = mockk<MessageApiService>()


    @Before
    fun setup() {
        repository = NetworkMessageRepository(messageApiService)
    }

    @Test
    fun `getById should return successful response with correct message`() = runTest {
        val messageId = "testMessageId"
        val expectedMessage = Message(_id = messageId, message = "Test message")
        val mockResponse = Response.success(expectedMessage)

        coEvery { messageApiService.getById(messageId) } returns mockResponse

        val result = repository.getById(messageId)

        assertTrue(result.isSuccessful)
        assertEquals(expectedMessage, result.body())
    }


    @Test
    fun `create should call messageApiService create with correct parameters for text message without files`() =
        runTest {
            val message = "Test message"
            val chatgroup = "testChatGroup"
            val expectedResponse = mockk<Response<Message>>()

            coEvery {
                messageApiService.create(
                    any(),
                    any(),
                    any(),
                    emptyList()
                )
            } returns expectedResponse

            val result = repository.create(message, chatgroup, emptyList(), emptyList())

            coVerify {
                messageApiService.create(
                    any(),
                    any(),
                    withArg { map ->
                        assertTrue(map is HashMap<String, RequestBody>)
                        assertTrue(map.isEmpty())
                    },
                    eq(emptyList())
                )
            }

            assertEquals(expectedResponse, result)
        }

    @Test
    fun `create should successfully create a new message with text content and system files`() =
        runTest {
            val messageApiService = mockk<MessageApiService>()
            val repository = NetworkMessageRepository(messageApiService)

            val message = "Test message"
            val chatgroup = "test_chatgroup_id"
            val systemFiles = listOf("file1.txt", "file2.txt")
            val files = emptyList<Pair<File, String>>()

            val expectedResponse = mockk<Response<Message>> {
                every { isSuccessful } returns true
                every { body() } returns Message(_id = "1", message = message)
            }

            coEvery {
                messageApiService.create(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns expectedResponse

            val result = repository.create(message, chatgroup, systemFiles, files)

            coVerify {
                messageApiService.create(
                    any(),
                    any(),
                    withArg<HashMap<String, RequestBody>> { map ->
                        assertEquals(2, map.size)
                        assertTrue(map.containsKey("files[0]"))
                        assertTrue(map.containsKey("files[1]"))
                    },
                    emptyList()
                )
            }

            assertEquals(expectedResponse, result)
        }


    @Test
    fun `create should send message with text content and user-uploaded files`() = runTest {
        val message = "Test message"
        val chatgroup = "testChatGroup"
        val systemFiles = listOf("file1.txt", "file2.txt")
        val files = listOf(
            Pair(File("path1.jpg"), "image/jpeg"),
            Pair(File("path2.png"), "image/png")
        )

        val expectedResponse = mockk<Response<Message>> {
            every { isSuccessful } returns true
            every { body() } returns Message(_id = "1", message = message)
        }

        coEvery {
            messageApiService.create(
                any(),
                any(),
                any(),
                any()
            )
        } returns expectedResponse

        val result = repository.create(message, chatgroup, systemFiles, files)

        coVerify {
            messageApiService.create(
                any(),
                any(),
                match<HashMap<String, RequestBody>> { map ->
                    map.size == 2 &&
                            map.keys.containsAll(listOf("files[0]", "files[1]")) &&
                            map.values.all { it is RequestBody }
                },
                match<List<MultipartBody.Part>> { parts ->
                    parts.size == 2 &&
                            parts.all { it is MultipartBody.Part }
                }
            )
        }

        assertEquals(expectedResponse, result)
    }


    @Test
    fun `create should call messageApiService create with correct parameters`() = runTest {
        val message = "Test message"
        val chatgroup = "testChatGroup"
        val systemFiles = listOf("file1.txt", "file2.txt")
        val files = listOf(
            Pair(File("path1.jpg"), "image/jpeg"),
            Pair(File("path2.pdf"), "application/pdf")
        )

        val mockResponse = mockk<Response<Message>>()
        coEvery {
            messageApiService.create(
                any(),
                any(),
                any(),
                any()
            )
        } returns mockResponse

        val result = repository.create(message, chatgroup, systemFiles, files)

        coVerify {
            messageApiService.create(
                withArg {
                    it.contentType().toString() == "text/plain" && it.contentLength() > 0
                },
                withArg {
                    it.contentType().toString() == "text/plain" && it.contentLength() > 0
                },
                withArg<HashMap<String, RequestBody>> { map ->
                    map.size == 2 &&
                            map.all { (key, value) ->
                                key.startsWith("files[") &&
                                        value.contentType().toString() == "text/plain"
                            }
                },
                withArg<List<MultipartBody.Part>> { parts ->
                    parts.size == 2 &&
                            parts.all {
                                it.body.contentType() in listOf(
                                    "image/jpeg".toMediaType(),
                                    "application/pdf".toMediaType()
                                )
                            }
                }
            )
        }

        assertEquals(mockResponse, result)
    }

    @Test
    fun `updateById should call messageApiService updateById with correct parameters`() = runTest {
        val id = "testId"
        val message = Message(_id = id, message = "Updated message")
        val mockResponse: Response<Message> = mockk {
            every { isSuccessful } returns true
            every { body() } returns message
        }

        coEvery { messageApiService.updateById(id, message) } returns mockResponse

        val repository = NetworkMessageRepository(messageApiService)
        val result = repository.updateById(id, message)

        coVerify { messageApiService.updateById(id, message) }
        assertEquals(mockResponse, result)
    }

    @Test
    fun `deleteById should call messageApiService deleteById with correct parameter`() = runTest {
        val messageId = "testMessageId"
        val mockResponse: Response<Void> = mockk {
            every { isSuccessful } returns true
        }
        coEvery { messageApiService.deleteById(messageId) } returns mockResponse

        val repository = NetworkMessageRepository(messageApiService)
        val result = repository.deleteById(messageId)

        coVerify(exactly = 1) { messageApiService.deleteById(messageId) }
        assertTrue(result.isSuccessful)
    }

    @Test
    fun `getAll should return all messages`() = runTest {
        // Arrange
        val mockMessages = listOf(
            Message(_id = "1", message = "Hello"),
            Message(_id = "2", message = "World")
        )
        val mockResponse = Response.success(mockMessages)
        coEvery { messageApiService.getAll() } returns mockResponse

        // Act
        val result = repository.getAll()

        // Assert
        assertTrue(result.isSuccessful)
        assertEquals(mockMessages, result.body())
        coVerify(exactly = 1) { messageApiService.getAll() }
    }

    @Test
    fun `create should handle empty system files list`() = runTest {
        val message = "Test message"
        val chatgroup = "test-chatgroup"
        val emptySystemFiles = emptyList<String>()
        val files = listOf(Pair(File("test.txt"), "text/plain"))

        val expectedResponse = mockk<Response<Message>>()

        coEvery {
            messageApiService.create(
                any(),
                any(),
                any(),
                any()
            )
        } returns expectedResponse

        val result = repository.create(message, chatgroup, emptySystemFiles, files)

        assertEquals(expectedResponse, result)
        coVerify {
            messageApiService.create(
                any(),
                any(),
                eq(HashMap()),
                any()
            )
        }
    }

}