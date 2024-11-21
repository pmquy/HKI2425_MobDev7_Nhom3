package com.example.facebook.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.facebook.FacebookApplication
import com.example.facebook.data.ChatGroupRepository
import com.example.facebook.model.ChatGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HomeUIState(
    val chatGroups: List<ChatGroup> = listOf(),
    val hasMore: Boolean = true,
    val offset: Int = 0,
)

class HomeViewModel(
    private val chatGroupRepository: ChatGroupRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUIState())
    val uiState = _uiState.asStateFlow()

    private val LIMIT = 10

    suspend fun getAll() {
        val response = chatGroupRepository.getAll(0, LIMIT, "{}")
        if (!response.isSuccessful) throw Exception("Error getting chat groups")
        _uiState.update {
            it.copy(
                chatGroups = response.body()!!.data, hasMore = response.body()!!.hasMore
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FacebookApplication)
                val chatGroupRepository = application.container.chatGroupRepository
                HomeViewModel(chatGroupRepository = chatGroupRepository)
            }
        }
    }

}