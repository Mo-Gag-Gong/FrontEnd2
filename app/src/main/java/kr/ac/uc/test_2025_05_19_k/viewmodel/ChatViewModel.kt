package kr.ac.uc.test_2025_05_19_k.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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

    companion object {
        private const val REFRESH_INTERVAL_MS = 3000L
    }

    val groupId: Long = savedStateHandle.get<Long>("groupId")!!
    val myUserId: Long? = tokenManager.getUserId()

    private val _messages = MutableStateFlow<List<GroupChatDto>>(emptyList())
    val messages: StateFlow<List<GroupChatDto>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchInitialChats()
        startPeriodicRefresh()
    }

    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(REFRESH_INTERVAL_MS)
                try {
                    // 백그라운드 새로고침이므로 로딩 상태를 표시하지 않음
                    val response = groupRepository.getGroupChats(groupId, 0)
                    val newMessages = response.content.reversed()

                    // 현재 메시지 목록과 다를 경우에만 UI를 업데이트하여 불필요한 깜빡임 방지
                    if (newMessages != _messages.value) {
                        _messages.value = newMessages
                    }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "채팅 자동 새로고침 실패", e)
                    // 오류가 발생해도 루프가 멈추지 않고 계속 시도
                }
            }
        }
    }

    private fun fetchInitialChats() {
        if (isLoading.value) return
        viewModelScope.launch {
            try {
                val response = groupRepository.getGroupChats(groupId, 0)
                _messages.value = response.content.reversed()
            } catch (e: Exception) {
                _error.value = "채팅 내역을 불러오는데 실패했습니다."
                Log.e("ChatViewModel", "Error fetching chats", e)
            } finally {
                _isLoading.value = false
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
                // 메시지 전송 성공 시, 즉시 최신 내역을 다시 불러옴
                fetchLatestMessagesAfterSend()
            } catch (e: Exception) {
                _error.value = "메시지 전송에 실패했습니다."
                Log.e("ChatViewModel", "Error sending message", e)
            }
        }
    }

    private fun fetchLatestMessagesAfterSend() {
        viewModelScope.launch {
            try {
                val response = groupRepository.getGroupChats(groupId, 0)
                _messages.value = response.content.reversed()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error fetching latest chats after send", e)
            }
        }
    }
}