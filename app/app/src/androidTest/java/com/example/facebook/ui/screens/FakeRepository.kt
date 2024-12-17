package com.example.facebook.ui.screens

import com.example.facebook.data.ChatGroupRepository
import com.example.facebook.data.FriendRepository
import com.example.facebook.data.MessageRepository
import com.example.facebook.data.UserRepository
import com.example.facebook.model.ChatGroup
import com.example.facebook.model.Friend
import com.example.facebook.model.GetAllChatGroupsResponse
import com.example.facebook.model.GetFriendResponse
import com.example.facebook.model.GetFriendSuggestionsResponse
import com.example.facebook.model.GetMessagesResponse
import com.example.facebook.model.GetUsersResponse
import com.example.facebook.model.Member
import com.example.facebook.model.Message
import com.example.facebook.model.User
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File

class FakeChatGroup : ChatGroupRepository {
    private val mockedChatGroup1 = ChatGroup(
        _id = "group1",
        name = "Test Group1",
        avatar = "672f78cd4763def725d6974f",
        createdAt = "2024-12-08T19:26:06.898Z",
        updatedAt = "2024-12-08T19:26:06.898Z",
        users = listOf(
            Member("user1", "admin", "2024-12-08T19:26:40.898Z"),
            Member("user2", "member", "2024-12-08T19:30:16.898Z")
        )
    )
    private val mockedChatGroup0 = ChatGroup(
        _id = "group2",
        name = "Test Group",
        avatar = "6755f2b80d0a71201c9ee387",
        createdAt = "2024-12-08T19:26:06.898Z",
        updatedAt = "2024-12-08T19:26:56.898Z",
        users = listOf(
            Member("user1", "member", "2024-12-08T19:26:06.898Z"),
            Member("user2", "admin", "2024-12-08T19:27:06.898Z")
        )
    )

    private val mockedMessages = listOf(
        Message(
            _id = "msg1",
            message = "Hello, group!",
            user = "user1",
            createdAt = "2024-12-08T19:30:00.000Z"
        ),
        Message(
            _id = "msg2",
            message = "Hi there!",
            user = "user2",
            createdAt = "2024-12-08T19:31:00.000Z"
        )
    )

    override suspend fun create(
        name: String,
        users: List<Member>,
        avatar: Pair<java.io.File, String>?
    ): Response<ChatGroup> {
        return Response.success(mockedChatGroup1)
    }

    override suspend fun getById(id: String): Response<ChatGroup> {
        return Response.success(mockedChatGroup1)
    }

    override suspend fun updateById(id: String, name: String, avatar: Pair<java.io.File, String>?): Response<ChatGroup> {
        return Response.success(mockedChatGroup1.copy(name = name))
    }

    override suspend fun deleteById(id: String): Response<Void> {
        return Response.success(null)
    }

    override suspend fun getMessage(id: String, offset: Int, limit: Int, query: String): Response<GetMessagesResponse> {
        return Response.success(GetMessagesResponse(mockedMessages, hasMore = false))
    }

    override suspend fun getMember(id: String): Response<List<Member>> {
        return Response.success(mockedChatGroup1.users)
    }

    override suspend fun addMember(id: String, members: List<Member>): Response<ChatGroup> {
        return Response.success(mockedChatGroup1.copy(users = mockedChatGroup1.users + members))
    }

    override suspend fun removeMember(id: String, member: Member): Response<ChatGroup> {
        return Response.success(mockedChatGroup1.copy(users = mockedChatGroup1.users.filter { it.user != member.user }))
    }

    override suspend fun updateMember(id: String, member: Member): Response<ChatGroup> {
        val updatedMembers = mockedChatGroup1.users.map { if (it.user == member.user) member else it }
        return Response.success(mockedChatGroup1.copy(users = updatedMembers))
    }

    override suspend fun getAll(offset: Int, limit: Int, query: String): Response<GetAllChatGroupsResponse> {
        return Response.success(GetAllChatGroupsResponse(data = listOf(mockedChatGroup1,mockedChatGroup0), hasMore = false))
    }
}

class FakeFriendRepository : FriendRepository {
    private val mockFriend1 = Friend(
        _id = "1",
        from = "user1",
        to = "user2",
        status = "accepted",
        createdAt = "2024-12-08T19:26:06.898Z",
        updatedAt = "2024-12-08T19:26:06.898Z")

    private val mockFriend2 = Friend(
        _id = "2",
        from = "user2",
        to = "user3",
        status = "pending",
        createdAt = "2024-12-08T19:26:06.898Z",
        updatedAt = "2024-12-08T19:26:06.898Z")

    private val mockFriends = listOf(mockFriend1, mockFriend2)
    override suspend fun request(to: String) {
        mockFriends.map { friend ->
            if (friend.to == to) {
                friend.status = "pending"
            }
        }
    }

    override suspend fun accept(from: String) {
        mockFriends.map { friend ->
            if (friend.from == from) {
                friend.status = "accepted"
            }
        }
    }

    override suspend fun decline(from: String) {
        mockFriends.map { friend ->
            if (friend.from == from) {
                friend.status = "declined"
            }
        }
    }

    override suspend fun revoke(to: String) {
        mockFriends.map { friend ->
            if (friend.to == to) {
                friend.status = "revoked"
            }
        }
    }

    override suspend fun disfriend(from: String) {
        mockFriends.map { friend ->
            if (friend.from == from) {
                friend.status = "disfriended"
            }
        }
    }

    override suspend fun getAll(
        offset: Int?,
        limit: Int?,
        q: String?
    ): Response<GetFriendResponse> {
        return Response.success(GetFriendResponse(data = mockFriends, hasMore = false))
    }

    override suspend fun getSuggestions(
        offset: Int?,
        limit: Int?,
        q: String?
    ): Response<GetFriendSuggestionsResponse> {
        val listFriends = mockFriends.filter { friend -> friend.status == "accepted" }
        val listFriendsId = listFriends.toMutableList().map { friend -> friend.to }
        return Response.success(GetFriendSuggestionsResponse(data = listFriendsId, hasMore = false))
    }
}
class fakeFriendsRepository : FriendRepository {
    val mockFrined1 = Friend(
        _id = "1",
        from = "user1",
        to = "user2",
        status = "accepted",
        createdAt = "2024-12-08T19:26:06.898Z",
        updatedAt = "2024-12-08T19:26:06.898Z"
    )

    val mockFriend2 = Friend(
        _id = "2",
        from = "user2",
        to = "user3",
        status = "pending",
        createdAt = "2024-12-08T19:26:06.898Z",
        updatedAt = "2024-12-08T19:26:06.898Z"
    )

    override suspend fun request(to: String) {
        TODO("Not yet implemented")
    }

    override suspend fun accept(from: String) {
        TODO("Not yet implemented")
    }

    override suspend fun decline(from: String) {
        TODO("Not yet implemented")
    }

    override suspend fun revoke(to: String) {
        TODO("Not yet implemented")
    }

    override suspend fun disfriend(from: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getAll(
        offset: Int?,
        limit: Int?,
        q: String?
    ): Response<GetFriendResponse> {
        return Response.success(GetFriendResponse(data = listOf(mockFrined1, mockFriend2), hasMore = false))
    }

    override suspend fun getSuggestions(
        offset: Int?,
        limit: Int?,
        q: String?
    ): Response<GetFriendSuggestionsResponse> {
        return Response.success(GetFriendSuggestionsResponse(data = listOf("user4", "user5"), hasMore = false))
    }
}

class FakeListMes : MessageRepository {
    private val mockedMessages = mutableListOf(
        Message(
            _id = "message1",
            message = "Hello, world!",
            chatgroup = "group1",
            user = "user1",
            createdAt = "2024-12-08T19:30:00.000Z",
            updatedAt = "2024-12-08T19:30:00.000Z"
        ),
        Message(
            _id = "message2",
            message = "How are you?",
            chatgroup = "group1",
            user = "user2",
            createdAt = "2024-12-08T19:31:00.000Z",
            updatedAt = "2024-12-08T19:31:00.000Z"
        )
    )

    override suspend fun getById(id: String): Response<Message> {
        val message = mockedMessages.find { it._id == id }
        return if (message != null) {
            Response.success(message)
        } else {
            Response.error(404, ResponseBody.create(null, "Message not found"))
        }
    }

    override suspend fun create(
        message: String,
        chatgroup: String,
        systemFiles: List<String>,
        files: List<Pair<java.io.File, String>>
    ): Response<Message> {
        val newMessage = Message(
            _id = (mockedMessages.size + 1).toString(),
            message = message,
            chatgroup = chatgroup,
            user = "currentUser",
            createdAt = "2024-12-08T20:00:00.000Z",
            updatedAt = "2024-12-08T20:00:00.000Z"
        )
        mockedMessages.add(newMessage)
        return Response.success(newMessage)
    }

    override suspend fun updateById(id: String, message: Message): Response<Message> {
        val index = mockedMessages.indexOfFirst { it._id == id }
        return if (index != -1) {
            mockedMessages[index] = message
            Response.success(message)
        } else {
            Response.error(404, ResponseBody.create(null, "Message not found"))
        }
    }

    override suspend fun deleteById(id: String): Response<Void> {
        val removed = mockedMessages.removeIf { it._id == id }
        return if (removed) {
            Response.success(null)
        } else {
            Response.error(404, ResponseBody.create(null, "Message not found"))
        }
    }

    override suspend fun getAll(): Response<List<Message>> {
        return Response.success(mockedMessages)
    }
}

class fakeUserRepository : UserRepository {
    private val mockedUser = User(
        _id = "user1",
        firstName = "test2",
        lastName = "mhias",
        email = "ssbkss1010@gmail.com",
        phoneNumber = "0937263813",
        avatar = "6755f2b80d0a71201c9ee387",
        password = "mahieu1010",
        createdAt = "2024-12-08T19:26:06.898Z",
        updatedAt = "2024-12-08T19:26:06.898Z"
    )

    private val mockedUser2 = User(
        _id = "user2",
        firstName = "test3",
        lastName = "mahias",
        email = "adefasf14@gmail.com",
        phoneNumber = "0937263814",
        avatar = "672f78cd4763def725d6974f",
        password = "test3010",
        createdAt = "2024-12-09T19:26:06.898Z",
        updatedAt = "2024-12-24T19:26:06.898Z"
    )
    private val mockedUser3 = User(
        _id = "user3",
        firstName = "test4",
        lastName = "hiua",
        email = "adefasf134455@gmail.com",
        phoneNumber = "0937263815",
        avatar = "672f78cd4763def725d69750",
        password = "test4010",
        createdAt = "2024-12-19T19:26:06.898Z",
        updatedAt = "2024-12-24T19:26:06.898Z"
    )

    private var mockedUsers = listOf(mockedUser, mockedUser2, mockedUser3)

    fun getAllUsers(): List<User> {
        return mockedUsers
    }
    override suspend fun login(
        email: String,
        password: String,
        token: String?,
        socketId: String?
    ): Response<User> {
        if(email == "ssbkss1010@gmail.com" && password == "mahieu1010") {
            return Response.success(mockedUser)
        } else {
            val errorResponseBody = ResponseBody.create(null, "Invalid credentials")
            return Response.error(500, errorResponseBody)
        }
    }

    override suspend fun auth(token: String?, socketId: String?): Response<User> {
        TODO("Not yet implemented")
    }

    override suspend fun getById(id: String): Response<User> {
        when (id) {
            "user1" -> return Response.success(mockedUser)
            "user2" -> return Response.success(mockedUser2)
            "user3" -> return Response.success(mockedUser3)
            else -> return Response.error(404, ResponseBody.create(null, "User not found"))
        }
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phoneNumber: String,
        avatar: Pair<File, String>?
    ): Response<User> {
        val newUser = User(
            _id = "User${(mockedUsers.size + 1).toString()}",
            firstName = firstName,
            lastName = lastName,
            email = email,
            phoneNumber = phoneNumber,
            avatar =  avatar?.second?: "",
            password = password,
            createdAt = java.time.Instant.now().toString(),
            updatedAt = java.time.Instant.now().toString()
        )
        mockedUsers = mockedUsers + newUser
        return Response.success(newUser)
    }

    override suspend fun verifyOtp(email: String, otp: String): Response<User> {
        TODO("Not yet implemented")
    }

    override suspend fun update(
        firstName: String?,
        lastName: String?,
        password: String?,
        phoneNumber: String?,
        avatar: Pair<File, String>?
    ): Response<User> {
        val updatedUsers = mockedUsers.map { user ->
            if (user._id in listOf("user1", "user2", "user3")) {
                user.copy(
                    firstName = firstName ?: user.firstName,
                    lastName = lastName ?: user.lastName,
                    password = password ?: user.password,
                    phoneNumber = phoneNumber ?: user.phoneNumber,
                    avatar = avatar?.second ?: user.avatar,
                    updatedAt = java.time.Instant.now().toString()
                )
            } else {
                user
            }
        }

        mockedUsers = updatedUsers
        val updatedUser = updatedUsers.find { it._id == "user1" }
        return if (updatedUser != null) {
            Response.success(updatedUser)
        } else {
            Response.error(404, ResponseBody.create(null, "User not found"))
        }
    }

    override suspend fun logout(): Response<Void> {
        TODO("Not yet implemented")
    }

    override suspend fun getUsers(offset: Int, limit: Int, q: String): Response<GetUsersResponse> {
        return Response.success(GetUsersResponse(data = mockedUsers, hasMore = false))
    }
}