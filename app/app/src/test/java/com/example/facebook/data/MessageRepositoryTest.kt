package com.example.facebook.data

import com.example.facebook.model.Message
import com.example.facebook.network.MessageApiService
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.File

class MessageRepositoryTest {

    @MockK
    private lateinit var messageApiService: MessageApiService

    private lateinit var messageRepository: MessageRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        messageRepository = NetworkMessageRepository(messageApiService)
    }

    @Test
    fun `getById should successfully retrieve a message by its ID`() = runTest {
        val mockMessage = mockk<Message>()
        val mockResponse = mockk<Response<Message>>()
        val messageId = "testMessageId"

        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body() } returns mockMessage

        coEvery { messageApiService.getById(messageId) } returns mockResponse

        val result = messageRepository.getById(messageId)

        assertTrue(result.isSuccessful)
        assertEquals(mockMessage, result.body())
        coVerify(exactly = 1) { messageApiService.getById(messageId) }
    }

    @Test
    fun `getById should return error response for non-existent ID`() = runTest {
        val mockMessageApiService = mockk<MessageApiService>()
        val repository = NetworkMessageRepository(mockMessageApiService)
        val nonExistentId = "non_existent_id"

        coEvery { mockMessageApiService.getById(nonExistentId) } returns Response.error(
            404,
            "Not Found".toResponseBody("text/plain".toMediaType())
        )

        val response = repository.getById(nonExistentId)

        assertFalse(response.isSuccessful)
        assertEquals(404, response.code())
    }

    @Test
    fun `create should return a valid response with all required parameters`() = runTest {
        val mockMessageApiService = mockk<MessageApiService>()
        val repository = NetworkMessageRepository(mockMessageApiService)

        val message = "Test message"
        val chatgroup = "testChatGroup"
        val systemFiles = listOf("file1.txt", "file2.txt")
        val files = listOf(
            Pair(File("path1.jpg"), "image/jpeg"),
            Pair(File("path2.pdf"), "application/pdf")
        )

        val expectedResponse = mockk<Response<Message>> {
            every { isSuccessful } returns true
            every { body() } returns Message(_id = "123", message = message)
        }

        coEvery {
            mockMessageApiService.create(
                any(),
                any(),
                any(),
                any()
            )
        } returns expectedResponse

        val result = repository.create(message, chatgroup, systemFiles, files)

        assertTrue(result.isSuccessful)
        assertEquals("123", result.body()?._id)
        assertEquals(message, result.body()?.message)

        coVerify {
            mockMessageApiService.create(
                any(),
                any(),
                withArg {
                    assertTrue(it is HashMap<*, *>)
                    assertEquals(systemFiles.size, it.size)
                    systemFiles.forEachIndexed { index, _ ->
                        assertTrue(it.containsKey("files[$index]"))
                    }
                },
                withArg {
                    assertEquals(files.size, it.size)
                    it.forEachIndexed { index, part ->
                        assertTrue(part is MultipartBody.Part)
                        assertTrue(
                            part.headers?.get("Content-Disposition")
                                ?.contains("name=\"files\"") == true
                        )
                        assertTrue(
                            part.headers?.get("Content-Disposition")
                                ?.contains("filename=\"${files[index].first.name}\"") == true
                        )
                    }
                }
            )
        }
    }

    @Test
    fun `create should handle empty system files and user files lists`() = runTest {
        val messageRepository = mockk<MessageRepository>()
        val message = "Test message"
        val chatgroup = "testChatGroup"
        val emptySystemFiles = emptyList<String>()
        val emptyUserFiles = emptyList<Pair<File, String>>()

        coEvery {
            messageRepository.create(message, chatgroup, emptySystemFiles, emptyUserFiles)
        } returns Response.success(Message(_id = "1", message = message, chatgroup = chatgroup))

        val response =
            messageRepository.create(message, chatgroup, emptySystemFiles, emptyUserFiles)

        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        assertEquals(message, response.body()?.message)
        assertEquals(chatgroup, response.body()?.chatgroup)
    }

    @Test
    fun `updateById should update an existing message with new content`() = runTest {
        val messageRepository = mockk<MessageRepository>()
        val messageId = "123"
        val updatedMessage = Message(_id = messageId, message = "Updated content")
        val response = Response.success(updatedMessage)

        coEvery { messageRepository.updateById(messageId, updatedMessage) } returns response

        val result = messageRepository.updateById(messageId, updatedMessage)

        assertTrue(result.isSuccessful)
        assertEquals(updatedMessage, result.body())
    }

    @Test
    fun `updateById should return error for non-existent message`() = runTest {
        val messageRepository = mockk<MessageRepository>()
        val nonExistentId = "non_existent_id"
        val updatedMessage = Message(_id = nonExistentId, message = "Updated content")

        coEvery {
            messageRepository.updateById(
                nonExistentId,
                updatedMessage
            )
        } returns Response.error(404, "Message not found".toResponseBody(null))

        val response = messageRepository.updateById(nonExistentId, updatedMessage)

        assertFalse(response.isSuccessful)
        assertEquals(404, response.code())
    }

    @Test
    fun `deleteById should return a Response of Void when deleting a message`() = runTest {
        val messageRepository = mockk<MessageRepository>()
        val messageId = "test_message_id"
        val expectedResponse = Response.success<Void>(null)

        coEvery { messageRepository.deleteById(messageId) } returns expectedResponse

        val actualResponse = messageRepository.deleteById(messageId)

        assertEquals(expectedResponse, actualResponse)
        coVerify(exactly = 1) { messageRepository.deleteById(messageId) }
    }

    @Test
    fun `deleteById should return an error for non-existent message`() = runTest {
        val mockMessageApiService = mockk<MessageApiService>()
        val messageRepository: MessageRepository = NetworkMessageRepository(mockMessageApiService)

        val nonExistentId = "non_existent_id"

        coEvery { mockMessageApiService.deleteById(nonExistentId) } returns Response.error(
            404,
            "Not Found".toResponseBody(null)
        )

        val response = messageRepository.deleteById(nonExistentId)

        assertFalse(response.isSuccessful)
        assertEquals(404, response.code())
    }

    @Test
    fun `getAll should retrieve all messages successfully`() = runTest {
        val mockMessageList = listOf(
            Message(_id = "1", message = "Test message 1"),
            Message(_id = "2", message = "Test message 2")
        )
        val mockResponse = mockk<Response<List<Message>>> {
            every { isSuccessful } returns true
            every { body() } returns mockMessageList
        }

        coEvery { messageApiService.getAll() } returns mockResponse

        val repository = NetworkMessageRepository(messageApiService)
        val result = repository.getAll()

        assertTrue(result.isSuccessful)
        assertEquals(mockMessageList, result.body())
    }

    @Test
    fun `create should handle large file attachments`() = runTest {
        val messageRepository = mockk<MessageRepository>()
        val largeFile = File("large_file.zip")
        val message = "Test message with large attachment"
        val chatgroup = "test_chatgroup"
        val systemFiles = listOf("system_file1.txt", "system_file2.txt")
        val files = listOf(Pair(largeFile, "application/zip"))

        coEvery {
            messageRepository.create(message, chatgroup, systemFiles, files)
        } returns Response.success(Message(_id = "1", message = message))

        val response = messageRepository.create(message, chatgroup, systemFiles, files)

        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        assertEquals(message, response.body()?.message)
    }
}

