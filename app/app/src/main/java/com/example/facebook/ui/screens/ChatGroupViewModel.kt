package com.example.facebook.ui.screens

import android.util.Log
import android.widget.Toast
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
import com.example.facebook.model.Member
import com.example.facebook.model.Message
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File


enum class InputType {
    DEFAULT,
    EMOTICON,
    IMAGE,
    FILE,
    MIC,
    NORMAL,
    TAKE_PICTURE,
}


data class ChatGroupUIState(
    val chatGroup: ChatGroup = ChatGroup(),
    val messages: List<Message> = listOf(),
    val hasMore: Boolean = true,
    val offset: Int = 0,
    var messageText: String = "",
    var systemFiles: List<String> = listOf(),
    var files: List<Pair<File, String>> = listOf(),
    var inputType: InputType = InputType.DEFAULT,
    val users: List<Member> = listOf(),
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
    private val LIMIT = 10

    private val onNewMessage = Emitter.Listener { args ->
        val message = Json.decodeFromString<Message>(args[0].toString())
        _uiState.update {
            it.copy(
                messages = it.messages + message,
                offset = it.offset + 1
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
            val response3 = chatGroupRepository.getMember(id)
            if (!response3.isSuccessful) throw Exception("User getting messages")
            Log.d("ChatGroupViewModel", "initChatGroup: ${response1.body()}")
            _uiState.value = _uiState.value.copy(
                chatGroup = response1.body()!!,
                messages = response2.body()!!.data,
                hasMore = response2.body()!!.hasMore,
                offset = LIMIT,
                users = response3.body()!!
            )
            Log.d("ChatGroupViewModel", "initChatGroup: ${_uiState.value}")
        }
    }

    fun getMoreMessages() {
        if (_uiState.value.chatGroup._id.isEmpty() || !_uiState.value.hasMore) return
        viewModelScope.launch {
            val response = chatGroupRepository.getMessage(
                _uiState.value.chatGroup._id,
                _uiState.value.offset,
                LIMIT,
                "{}"
            )
            if (!response.isSuccessful) throw Exception("Error getting messages")
            response.body()?.let { t ->
                _uiState.update {
                    it.copy(
                        messages = t.data + it.messages,
                        hasMore = t.hasMore,
                        offset = it.offset + LIMIT
                    )
                }
            }
        }
    }

    fun setSystemFiles(files: List<String>) {
        _uiState.update {
            it.copy(systemFiles = files)
        }
    }

    fun setFiles(files: List<Pair<File, String>>) {
        _uiState.update {
            it.copy(files = files)
        }
    }

    fun setInputType(inputType: InputType) {
        _uiState.update {
            it.copy(inputType = inputType)
        }
    }

    fun setMessageText(messageText: String) {
        _uiState.update {
            it.copy(messageText = messageText)
        }
    }

    fun createMessage(
        messageText: String,
        files: List<Pair<File, String>>,
        systemFiles: List<String>
    ) {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        inputType = InputType.DEFAULT
                    )
                }
                val response = messageRepository.create(
                    messageText,
                    _uiState.value.chatGroup._id,
                    systemFiles,
                    files
                )
                _uiState.update {
                    it.copy(
                        messageText = "",
                        files = listOf(),
                        systemFiles = listOf(),
                    )
                }
                if (!response.isSuccessful) throw Exception("Error creating message")
            } catch (e: Exception) {
                Toast.makeText(application, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun handleUpdate(
        name: String? = null,
        avatar: Pair<File, String>? = null,
    ) {
        viewModelScope.launch {
            try {
                val updatedName = name ?: _uiState.value.chatGroup.name
                val response = chatGroupRepository.updateById(
                    _uiState.value.chatGroup._id,
                    updatedName,
                    avatar
                )
                if (!response.isSuccessful) throw Exception("Error updating chat group")
                _uiState.update {
                    it.copy(chatGroup = response.body()!!)
                }
            } catch (e: Exception) {
                Toast.makeText(application, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun currentUserId(): String {
        return application.user._id
    }

    fun addMember(user: String, role: String = "member") {
        _uiState.value = _uiState.value.copy(users = _uiState.value.users + Member(user, role))
    }

    fun checkMember(user: String): Boolean {
        return _uiState.value.users.any { it.user == user }
    }

    fun removeMember(user: String) {
        _uiState.value =
            _uiState.value.copy(users = _uiState.value.users.filter { it.user != user })
    }

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