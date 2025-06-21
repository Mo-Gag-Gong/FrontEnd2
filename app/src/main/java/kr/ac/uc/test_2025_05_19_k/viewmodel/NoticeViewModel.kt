package kr.ac.uc.test_2025_05_19_k.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.model.request.GroupNoticeCreateRequest
import kr.ac.uc.test_2025_05_19_k.repository.GroupRepository
import javax.inject.Inject

sealed class NoticeFormEvent {
    data class ShowToast(val message: String) : NoticeFormEvent()
    data object NavigateBack : NoticeFormEvent()
}

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val groupId: Long = savedStateHandle.get<Long>("groupId") ?: -1L
    private val noticeId: Long = savedStateHandle.get<Long>("noticeId") ?: -1L

    val isEditMode = noticeId != -1L

    var title by mutableStateOf("")
    var content by mutableStateOf("")

    var isLoading by mutableStateOf(false)
        private set

    private val _eventFlow = MutableSharedFlow<NoticeFormEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        if (isEditMode) {
            // ▼▼▼ [수정] SavedStateHandle에서 사용하는 키 값을 "title", "content"로 변경 ▼▼▼
            title = savedStateHandle.get<String>("title") ?: ""
            content = savedStateHandle.get<String>("content") ?: ""
        }
    }

    fun saveNotice() {
        if (title.isBlank() || content.isBlank()) {
            viewModelScope.launch { _eventFlow.emit(NoticeFormEvent.ShowToast("제목과 내용을 모두 입력해주세요.")) }
            return
        }
        if (groupId == -1L) {
            viewModelScope.launch { _eventFlow.emit(NoticeFormEvent.ShowToast("잘못된 정보입니다.")) }
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                val request = GroupNoticeCreateRequest(title, content)
                if (isEditMode) {
                    groupRepository.updateNotice(groupId, noticeId, request)
                    _eventFlow.emit(NoticeFormEvent.ShowToast("공지사항이 수정되었습니다."))
                } else {
                    groupRepository.createNotice(groupId, request)
                    _eventFlow.emit(NoticeFormEvent.ShowToast("공지사항이 등록되었습니다."))
                }
                _eventFlow.emit(NoticeFormEvent.NavigateBack)
            } catch (e: Exception) {
                Log.e("NoticeVM", "Save failed", e)
                _eventFlow.emit(NoticeFormEvent.ShowToast("저장에 실패했습니다: ${e.message}"))
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteNotice() {
        if (!isEditMode) return

        viewModelScope.launch {
            isLoading = true
            try {
                groupRepository.deleteNotice(groupId, noticeId)
                _eventFlow.emit(NoticeFormEvent.ShowToast("공지사항이 삭제되었습니다."))
                _eventFlow.emit(NoticeFormEvent.NavigateBack)
            } catch (e: Exception) {
                Log.e("NoticeVM", "Delete failed", e)
                _eventFlow.emit(NoticeFormEvent.ShowToast("삭제에 실패했습니다: ${e.message}"))
            } finally {
                isLoading = false
            }
        }
    }
}