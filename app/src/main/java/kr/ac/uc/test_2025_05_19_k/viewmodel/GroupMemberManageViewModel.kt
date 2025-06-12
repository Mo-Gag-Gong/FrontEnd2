package kr.ac.uc.test_2025_05_19_k.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.model.GroupMemberDto
import kr.ac.uc.test_2025_05_19_k.repository.GroupRepository
import javax.inject.Inject

@HiltViewModel
class GroupMemberManageViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val groupId: Long = checkNotNull(savedStateHandle["groupId"])

    private val _pendingMembers = MutableStateFlow<List<GroupMemberDto>>(emptyList())
    val pendingMembers: StateFlow<List<GroupMemberDto>> = _pendingMembers.asStateFlow()

    init {
        fetchPendingMembers()
    }

    fun fetchPendingMembers() {
        viewModelScope.launch {
            groupRepository.getPendingMembers(groupId).onSuccess { members ->
                _pendingMembers.value = members
            }.onFailure {
                // 오류 처리
            }
        }
    }

    // approve/reject 함수는 private으로 유지해도 괜찮습니다.
    // 현재 UI에서는 사용하지 않기 때문입니다.
    fun approveMember(userId: Long) {
        viewModelScope.launch {
            groupRepository.approveMember(groupId, userId).onSuccess {
                fetchPendingMembers()
            }.onFailure {
                // 오류 처리
            }
        }
    }

    fun rejectMember(userId: Long) {
        viewModelScope.launch {
            groupRepository.rejectMember(groupId, userId).onSuccess {
                fetchPendingMembers()
            }.onFailure {
                // 오류 처리
            }
        }
    }
}