package kr.ac.uc.test_2025_05_19_k.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.model.UserProfileWithStatsDto
import kr.ac.uc.test_2025_05_19_k.repository.GroupRepository
import kr.ac.uc.test_2025_05_19_k.repository.TokenManager
import kr.ac.uc.test_2025_05_19_k.repository.UserRepository
import javax.inject.Inject

// [수정] 액션 결과를 UI에 전달하기 위한 데이터 클래스
data class ActionResult(val isSuccess: Boolean, val message: String)

@HiltViewModel
class GroupMemberDetailViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val tokenManager: TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val groupId: Long = checkNotNull(savedStateHandle["groupId"])
    val userId: Long = checkNotNull(savedStateHandle["userId"])
    val status: String = checkNotNull(savedStateHandle["status"])

    private val _userProfile = MutableStateFlow<UserProfileWithStatsDto?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _isOwner = MutableStateFlow(false)
    val isOwner = _isOwner.asStateFlow()

    private val _loggedInUserId = MutableStateFlow<Long?>(null)
    val loggedInUserId = _loggedInUserId.asStateFlow()

    private val _actionResultEvent = MutableSharedFlow<ActionResult>()
    val actionResultEvent = _actionResultEvent.asSharedFlow()

    init {
        loadUserProfile()
        checkIfOwner()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            userRepository.getUserProfile(userId)
                .onSuccess { _userProfile.value = it }
                .onFailure { e ->
                    Log.e("GroupMemberDetailVM", "사용자 프로필 로드 실패", e)
                    _userProfile.value = null
                }
        }
    }

    private fun checkIfOwner() {
        viewModelScope.launch {
            try {
                Log.d("GroupMemberDetailVM", "checkIfOwner 시작. Group ID: $groupId")
                val currentUserId = tokenManager.getUserId()

                _loggedInUserId.value = currentUserId // [추가] 상태 변수에 로그인한 사용자 ID 저장

                val groupDetail = groupRepository.getGroupDetail(groupId)
                Log.d("GroupMemberDetailVM", "현재 사용자 ID: $currentUserId, 그룹장 ID: ${groupDetail.creatorId}")
                val isOwnerResult = (currentUserId != null && currentUserId == groupDetail.creatorId)
                _isOwner.value = isOwnerResult
                Log.d("GroupMemberDetailVM", "그룹장 여부 확인 결과: $isOwnerResult")
            } catch (e: Exception) {
                Log.e("GroupMemberDetailVM", "그룹장 여부 확인 중 예외 발생", e)
                _isOwner.value = false
            }
        }
    }

    fun kickMember() {
        viewModelScope.launch {
            groupRepository.kickMember(groupId, userId)
                .onSuccess { _actionResultEvent.emit(ActionResult(true, "멤버를 추방했습니다.")) }
                .onFailure { _actionResultEvent.emit(ActionResult(false, "추방에 실패했습니다.")) }
        }
    }

    fun approveMember() {
        viewModelScope.launch {
            groupRepository.approveMember(groupId, userId)
                .onSuccess { _actionResultEvent.emit(ActionResult(true, "가입을 승인했습니다.")) }
                .onFailure { _actionResultEvent.emit(ActionResult(false, "승인에 실패했습니다. (오류 코드: 404)")) }
        }
    }

    fun rejectMember() {
        viewModelScope.launch {
            groupRepository.rejectMember(groupId, userId)
                .onSuccess { _actionResultEvent.emit(ActionResult(true, "가입을 거절했습니다.")) }
                .onFailure { _actionResultEvent.emit(ActionResult(false, "거절에 실패했습니다. (오류 코드: 404)")) }
        }
    }
}