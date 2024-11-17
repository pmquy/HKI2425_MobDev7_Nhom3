package com.example.facebook.ui.screens

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.facebook.FacebookApplication
import com.example.facebook.data.FriendRepository
import com.example.facebook.model.Friend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FriendsViewModel(
    private val friendRepository: FriendRepository,
    private val application: FacebookApplication,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FriendsUIState())
    val uiState = _uiState.asStateFlow()

    fun request(to: String) {
        try {
            viewModelScope.launch {
                friendRepository.request(to)
            }
        } catch (e: Exception) {
            Toast.makeText(application, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun accept(from: String) {
        try {
            viewModelScope.launch {
                friendRepository.accept(from)
            }
        } catch (e: Exception) {
            Toast.makeText(application, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun decline(from: String) {
        try {
            viewModelScope.launch {
                friendRepository.decline(from)
            }
        } catch (e: Exception) {
            Toast.makeText(application, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun revoke(to: String) {
        try {
            viewModelScope.launch {
                friendRepository.revoke(to)
            }
        } catch (e: Exception) {
            Toast.makeText(application, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun disfriend(from: String) {
        try {
            viewModelScope.launch {
                friendRepository.disfriend(from)
            }
        } catch (e: Exception) {
            Toast.makeText(application, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun getRequests() {

        try {
            viewModelScope.launch {
                val response = friendRepository.getAll(
                    0,
                    100,
                    Json.encodeToString(mapOf("status" to "pending", "to" to application.user?._id))
                )
                if (!response.isSuccessful) throw Exception("Error getting requests")
                _uiState.update {
                    it.copy(
                        requests = it.requests + response.body()!!.data,
                    )
                }
            }
        } catch (e: Exception) {
            Toast.makeText(application, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun getSends() {

        try {
            viewModelScope.launch {
                val response = friendRepository.getAll(
                    0,
                    100,
                    Json.encodeToString(
                        mapOf(
                            "status" to "pending",
                            "from" to application.user?._id
                        )
                    )
                )
                if (!response.isSuccessful) throw Exception("Error getting sends")
                _uiState.update {
                    it.copy(
                        sends = it.sends + response.body()!!.data,
                    )
                }
            }
        } catch (e: Exception) {
            Toast.makeText(application, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun getFriends() {

        try {
            viewModelScope.launch {
                val response = friendRepository.getAll(
                    0,
                    100,
                    Json.encodeToString(mapOf("status" to "accepted"))
                )
                if (!response.isSuccessful) throw Exception("Error getting friends")
                val friends = response.body()!!
                friends.data.forEach { it.from = if (it.from == application.user?._id) it.to else it.from }

                _uiState.update {
                    it.copy(
                        friends = it.friends + friends.data
                    )
                }
            }
        } catch (e: Exception) {
            Toast.makeText(application, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun getSuggestions() {

        try {
            viewModelScope.launch {
                val response = friendRepository.getSuggestions(
                    0,
                    100,
                    "{}"
                )
                if (!response.isSuccessful) throw Exception("Error getting suggestions")
                _uiState.update {
                    it.copy(
                        suggestions = it.suggestions + response.body()!!.data,
                    )
                }
            }
        } catch (e: Exception) {
            Toast.makeText(application, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun changeSubScreen(subScreen: FriendSubScreen) {
        _uiState.update {
            it.copy(
                currentSubScreen = subScreen
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FacebookApplication)
                val friendRepository = application.container.friendRepository
                FriendsViewModel(
                    friendRepository = friendRepository,
                    application = application
                )
            }
        }
    }
}

data class FriendsUIState(
    val currentSubScreen: FriendSubScreen = FriendSubScreen.SUGGESTS,
    val requests: List<Friend> = listOf(),
    val sends: List<Friend> = listOf(),
    val friends: List<Friend> = listOf(),
    val suggestions: List<String> = listOf(),
)

enum class FriendSubScreen {
    SUGGESTS,
    REQUESTS,
    ALL
}