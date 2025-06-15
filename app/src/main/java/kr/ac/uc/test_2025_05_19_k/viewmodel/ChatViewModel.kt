package kr.ac.uc.test_2025_05_19_k.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.model.GroupChatDto
import kr.ac.uc.test_2025_05_19_k.model.request.GroupChatCreateRequest
import kr.ac.uc.test_2025_05_19_k.repository.GroupRepository
import kr.ac.uc.test_2025_05_19_k.repository.TokenManager
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val tokenManager: TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val groupId: Long = savedStateHandle.get<Long>("groupId")!!
    val myUserId: Long? = tokenManager.getUserId()

    private val _messages = MutableStateFlow<List<GroupChatDto>>(emptyList())
    val messages: StateFlow<List<GroupChatDto>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    init {
        fetchInitialChats()
    }

    private fun fetchInitialChats() {
        viewModelScope.launch {
            try {
                val response = groupRepository.getGroupChats(groupId, 0)
                _messages.value = response.content.reversed()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error fetching chats", e)
            }
        }
    }

    fun onInputTextChanged(newText: String) {
        _inputText.value = newText
    }

    fun sendChatMessage() {
        if (inputText.value.isBlank()) return

        viewModelScope.launch {
            val request = GroupChatCreateRequest(message = inputText.value)
            _inputText.value = ""
            try {
                groupRepository.sendChatMessage(groupId, request)
                fetchLatestMessages()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
            }
        }
    }

    private fun fetchLatestMessages() {
        viewModelScope.launch {
            try {
                val response = groupRepository.getGroupChats(groupId, 0)
                _messages.value = response.content.reversed()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error fetching latest chats", e)
            }
        }
    }
}