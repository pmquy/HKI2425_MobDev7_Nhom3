package com.example.facebook.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.facebook.FacebookApplication
import com.example.facebook.data.ChatGroupRepository
import com.example.facebook.data.MessageRepository
import com.example.facebook.data.SocketRepository
import com.example.facebook.data.UserRepository
import com.example.facebook.model.ChatGroup
import com.example.facebook.model.Message
import com.example.facebook.model.User
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import retrofit2.Response
import java.io.File

val LIMIT = 10;

data class ChatGroupUIState(
    val chatGroup: ChatGroup = ChatGroup(),
    val messages: List<Message> = listOf(),
    val hasMore: Boolean = true,
    val page: Int = 0,
    val scrollState: ScrollState = ScrollState(0),
    val newMessage: Int = 0
)

class ChatGroupViewModel(
    private val application: FacebookApplication,
    private val chatGroupRepository: ChatGroupRepository,
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository,
    private val socketRepository: SocketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatGroupUIState())
    val uiState = _uiState.asStateFlow()

    private val onNewMessage = Emitter.Listener { args ->
        val message = Json.decodeFromString<Message>(args[0].toString())
        _uiState.update {
            it.copy(
                messages = it.messages + message,
                newMessage = it.newMessage + 1
            )
        }
    }

    fun isMine(message: Message): Boolean {
        return message.user == application.user._id
    }

    fun initChatGroup(id: String) {
        viewModelScope.launch {
            socketRepository.addEventListener("new_message", onNewMessage)
            socketRepository.sendMessage("join", "chatgroup", id)
            val response1 = chatGroupRepository.getById(id)
            if (!response1.isSuccessful) throw Exception("Error getting chat group")
            val response2 = chatGroupRepository.getMessage(id, 0, LIMIT, "{}")
            if (!response2.isSuccessful) throw Exception("Error getting messages")
            _uiState.value = _uiState.value.copy(
                chatGroup = response1.body()!!,
                messages = response2.body()!!.data,
                hasMore = response2.body()!!.hasMore,
                page = 1,
            )
        }
    }

    fun getMoreMessages() {
        if (_uiState.value.chatGroup._id.isEmpty() || !_uiState.value.hasMore) return
        viewModelScope.launch {
            val response = chatGroupRepository.getMessage(
                _uiState.value.chatGroup._id,
                _uiState.value.page,
                LIMIT,
                "{}"
            )
            if (!response.isSuccessful) throw Exception("Error getting messages")
            _uiState.update {
                it.copy(
                    messages = response.body()!!.data + it.messages,
                    hasMore = response.body()!!.hasMore,
                    page = it.page + 1
                )
            }
        }
    }

    fun createMessage(message: String, files: List<File>) {
        viewModelScope.launch {
            val response = messageRepository.create(
                message,
                _uiState.value.chatGroup._id,
                files
            )
            if (!response.isSuccessful) throw Exception("Error creating message")
        }
    }

    suspend fun getUserById(id: String): Response<User> = userRepository.getById(id)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FacebookApplication)
                val chatGroupRepository = application.container.chatGroupRepository
                val userRepository = application.container.userRepository
                val messageRepository = application.container.messageRepository
                val socketRepository = application.container.socketRepository
                ChatGroupViewModel(
                    application = application,
                    chatGroupRepository = chatGroupRepository,
                    userRepository = userRepository,
                    messageRepository = messageRepository,
                    socketRepository = socketRepository
                )
            }
        }
    }

}