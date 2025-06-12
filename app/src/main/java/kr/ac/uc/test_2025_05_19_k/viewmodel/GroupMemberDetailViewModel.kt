package kr.ac.uc.test_2025_05_19_k.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.model.UserProfileWithStatsDto
import kr.ac.uc.test_2025_05_19_k.repository.GroupRepository
import kr.ac.uc.test_2025_05_19_k.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class GroupMemberDetailViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val userId: Long = checkNotNull(savedStateHandle["userId"])
    private val groupId: Long = checkNotNull(savedStateHandle["groupId"])
    val status: String = checkNotNull(savedStateHandle["status"]) // status 값 받기

    private val _memberInfo = MutableStateFlow<UserProfileWithStatsDto?>(null)
    val memberInfo: StateFlow<UserProfileWithStatsDto?> = _memberInfo.asStateFlow()

    init {
        fetchMemberInfo()
    }

    private fun fetchMemberInfo() {
        viewModelScope.launch {
            userRepository.getUserProfile(userId).onSuccess {
                _memberInfo.value = it
            }
        }
    }

    // 기존 kickMember 함수
    fun kickMember(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            groupRepository.kickMember(groupId, userId).onSuccess {
                onResult(true)
            }.onFailure {
                onResult(false)
            }
        }
    }

    // approveMember 함수 추가
    fun approveMember(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            groupRepository.approveMember(groupId, userId).onSuccess {
                onResult(true)
            }.onFailure {
                onResult(false)
            }
        }
    }

    // rejectMember 함수 추가
    fun rejectMember(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            groupRepository.rejectMember(groupId, userId).onSuccess {
                onResult(true)
            }.onFailure {
                onResult(false)
            }
        }
    }
}