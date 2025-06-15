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
import kr.ac.uc.test_2025_05_19_k.model.GroupGoalDto
import kr.ac.uc.test_2025_05_19_k.repository.GroupRepository
import kr.ac.uc.test_2025_05_19_k.repository.TokenManager
import javax.inject.Inject

@HiltViewModel
class GroupGoalDetailViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val tokenManager: TokenManager, // TokenManager 주입
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // [수정] NavArgument에서 String 타입으로 받도록 수정
    private val groupId: String = savedStateHandle.get<String>("groupId")!!
    private val goalId: String = savedStateHandle.get<String>("goalId")!!

    private val _goalDetail = MutableStateFlow<GroupGoalDto?>(null)
    val goalDetail: StateFlow<GroupGoalDto?> = _goalDetail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ▼▼▼ [추가] 현재 사용자가 그룹장인지 여부를 저장하는 상태 추가 ▼▼▼
    private val _isCurrentUserAdmin = MutableStateFlow(false)
    val isCurrentUserAdmin: StateFlow<Boolean> = _isCurrentUserAdmin.asStateFlow()

    init {
        loadGoalDetails()
    }

    fun loadGoalDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // ▼▼▼ [수정] 그룹장 여부 확인 로직 추가 ▼▼▼
                // 1. 그룹 상세 정보를 가져와서 creatorId를 확인합니다.
                val groupDetail = groupRepository.getGroupDetail(groupId.toLong())
                val currentUserId = tokenManager.getUserId()
                _isCurrentUserAdmin.value = (groupDetail.creatorId == currentUserId)

                // 2. 기존처럼 목표 상세 정보를 가져옵니다.
                val detail = groupRepository.getGoalDetails(groupId, goalId)
                _goalDetail.value = detail

            } catch (e: Exception) {
                _error.value = "목표 상세 정보를 불러오는 데 실패했습니다."
                Log.e("GroupGoalDetailVM", "Failed to load details", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleDetailCompletion(detailId: Long?) {
        // ▼▼▼ [추가] 그룹장이 아닐 경우, 함수 실행을 막습니다. ▼▼▼
        if (!_isCurrentUserAdmin.value) {
            Log.w("GroupGoalDetailVM", "Permission denied: Only admin can toggle completion.")
            return
        }
        if (detailId == null) {
            Log.e("GoalDetailViewModel", "Cannot toggle completion for null detailId")
            return
        }

        viewModelScope.launch {
            try {
                groupRepository.toggleGoalDetail(groupId, goalId, detailId.toString())
                loadGoalDetails() // 성공 시 최신 정보로 갱신
            } catch (e: Exception) {
                Log.e("GoalDetailViewModel", "세부 목표 상태 변경 실패", e)
                _error.value = "상태 변경에 실패했습니다. 다시 시도해주세요."
            }
        }
    }

    fun deleteGoal(onSuccess: () -> Unit) {
        // ▼▼▼ [추가] 그룹장이 아닐 경우, 함수 실행을 막습니다. ▼▼▼
        if (!_isCurrentUserAdmin.value) {
            Log.w("GroupGoalDetailVM", "Permission denied: Only admin can delete goal.")
            _error.value = "목표를 삭제할 권한이 없습니다."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                groupRepository.deleteGoal(groupId, goalId)
                Log.d("GoalDetailViewModel", "목표 삭제 성공")
                onSuccess()
            } catch (e: Exception) {
                Log.e("GoalDetailViewModel", "목표 삭제 실패", e)
                _error.value = "목표 삭제에 실패했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }
}