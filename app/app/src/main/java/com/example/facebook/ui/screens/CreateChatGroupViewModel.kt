package com.example.facebook.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.facebook.FacebookApplication
import com.example.facebook.data.ChatGroupRepository
import com.example.facebook.model.Member
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class CreateChatGroupUIState(
    val name: String = "",
    val users: List<Member> = listOf(),
    val avatar: Pair<File, String>? = null,
)

class CreateChatGroupViewModel(
    private val application: FacebookApplication,
    private val chatGroupRepository: ChatGroupRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateChatGroupUIState())
    val uiState = _uiState.asStateFlow()

    fun setName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun setAvatar(avatar: Pair<File, String>) {
        _uiState.value = _uiState.value.copy(avatar = avatar)
    }

    suspend fun createChatGroup() {
        if (_uiState.value.name.isEmpty()) throw Exception("Name is empty")
        if (_uiState.value.users.isEmpty()) throw Exception("Members is empty")

        chatGroupRepository.create(
            name = _uiState.value.name,
            users = _uiState.value.users,
            avatar = _uiState.value.avatar,
        )
    }

    fun addMember(user: String, role: String = "member") {
        _uiState.value = _uiState.value.copy(users = _uiState.value.users + Member(user, role))
    }

    fun removeMember(user: String) {
        _uiState.value =
            _uiState.value.copy(users = _uiState.value.users.filter { it.user != user })
    }

    fun checkMember(user: String): Boolean {
        return _uiState.value.users.any { it.user == user }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FacebookApplication)
                val chatGroupRepository = application.container.chatGroupRepository
                CreateChatGroupViewModel(
                    application = application,
                    chatGroupRepository = chatGroupRepository,
                )
            }
        }
    }
}