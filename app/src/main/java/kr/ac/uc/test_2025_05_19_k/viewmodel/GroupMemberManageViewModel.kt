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
import kr.ac.uc.test_2025_05_19_k.model.GroupMemberDto
import kr.ac.uc.test_2025_05_19_k.model.UserProfileWithStatsDto
import kr.ac.uc.test_2025_05_19_k.repository.GroupRepository
import kr.ac.uc.test_2025_05_19_k.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class GroupMemberManageViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository, // UserRepository 주입
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val groupId: Long = checkNotNull(savedStateHandle["groupId"])

    private val _pendingMembers = MutableStateFlow<List<GroupMemberDto>>(emptyList())
    val pendingMembers: StateFlow<List<GroupMemberDto>> = _pendingMembers.asStateFlow()

    // ▼▼▼ [추가] 선택된 멤버의 프로필을 관리하는 상태 변수 ▼▼▼
    private val _selectedMemberProfile = MutableStateFlow<UserProfileWithStatsDto?>(null)
    val selectedMemberProfile: StateFlow<UserProfileWithStatsDto?> = _selectedMemberProfile.asStateFlow()

    init {
        fetchPendingMembers()
    }

    fun fetchPendingMembers() {
        viewModelScope.launch {
            groupRepository.getPendingMembers(groupId).onSuccess { members ->
                _pendingMembers.value = members
            }.onFailure {
                Log.e("MemberManageVM", "Failed to fetch pending members", it)
            }
        }
    }

    fun approveMember(userId: Long) {
        viewModelScope.launch {
            groupRepository.approveMember(groupId, userId).onSuccess {
                fetchPendingMembers() // 성공 시 목록 새로고침
            }.onFailure {
                Log.e("MemberManageVM", "Failed to approve member", it)
            }
        }
    }

    fun rejectMember(userId: Long) {
        viewModelScope.launch {
            groupRepository.rejectMember(groupId, userId).onSuccess {
                fetchPendingMembers() // 성공 시 목록 새로고침
            }.onFailure {
                Log.e("MemberManageVM", "Failed to reject member", it)
            }
        }
    }

    // ▼▼▼ [추가] 멤버 클릭 시 상세 프로필을 불러오는 함수 ▼▼▼
    fun onMemberSelected(userId: Long) {
        viewModelScope.launch {
            _selectedMemberProfile.value = null
            userRepository.getUserProfile(userId)
                .onSuccess { profile ->
                    _selectedMemberProfile.value = profile
                }
                .onFailure { e ->
                    Log.e("MemberManageVM", "Failed to get user profile", e)
                }
        }
    }

    // ▼▼▼ [추가] 멤버 정보 시트를 닫는 함수 ▼▼▼
    fun clearSelectedMember() {
        _selectedMemberProfile.value = null
    }
}