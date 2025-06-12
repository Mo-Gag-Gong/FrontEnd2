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
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: Long = checkNotNull(savedStateHandle["groupId"])

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

    fun approveMember(userId: Long) {
        viewModelScope.launch {
            groupRepository.approveMember(groupId, userId).onSuccess {
                fetchPendingMembers() // 성공 시 목록 새로고침
            }.onFailure {
                // 오류 처리
            }
        }
    }

    fun rejectMember(userId: Long) {
        viewModelScope.launch {
            groupRepository.rejectMember(groupId, userId).onSuccess {
                fetchPendingMembers() // 성공 시 목록 새로고침
            }.onFailure {
                // 오류 처리
            }
        }
    }
}