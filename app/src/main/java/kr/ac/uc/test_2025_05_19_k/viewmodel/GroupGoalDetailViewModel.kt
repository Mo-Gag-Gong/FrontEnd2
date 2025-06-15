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
import javax.inject.Inject

@HiltViewModel
class GroupGoalDetailViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>("groupId")!!
    private val goalId: String = savedStateHandle.get<String>("goalId")!!

    private val _goalDetail = MutableStateFlow<GroupGoalDto?>(null)
    val goalDetail: StateFlow<GroupGoalDto?> = _goalDetail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _goals = MutableStateFlow<List<GroupGoalDto>>(emptyList())
    val goals: StateFlow<List<GroupGoalDto>> = _goals.asStateFlow()

    private val _isLoadingGoals = MutableStateFlow(false)
    val isLoadingGoals: StateFlow<Boolean> = _isLoadingGoals.asStateFlow()

    val isAdmin: Boolean = savedStateHandle.get<Boolean>("isAdmin") ?: true

    init {
        loadGoalDetails()
    }

    fun loadGoalDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val detail = groupRepository.getGoalDetails(groupId, goalId)
                _goalDetail.value = detail
            } catch (e: Exception) {
                _error.value = "목표 상세 정보를 불러오는 데 실패했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleDetailCompletion(detailId: Long?) {
        if (detailId == null) {
            Log.e("GoalDetailViewModel", "Cannot toggle completion for null detailId")
            return
        }

        viewModelScope.launch {
            try {
                groupRepository.toggleGoalDetail(groupId, goalId, detailId.toString())
                loadGoalDetails()
            } catch (e: Exception) {
                Log.e("GoalDetailViewModel", "세부 목표 상태 변경 실패", e)
                _error.value = "상태 변경에 실패했습니다. 다시 시도해주세요."
            }
        }
    }


    fun deleteGoal(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                groupRepository.deleteGoal(groupId, goalId)
                Log.d("GoalDetailViewModel", "목표 삭제 성공")
                onSuccess() // 성공 콜백 호출 (화면에서 popBackStack 처리)
            } catch (e: Exception) {
                Log.e("GoalDetailViewModel", "목표 삭제 실패", e)
                _error.value = "목표 삭제에 실패했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchGroupGoals(forceRefresh: Boolean = false) {
        if (!forceRefresh && (_isLoadingGoals.value || _goals.value.isNotEmpty())) return

        viewModelScope.launch {
            _isLoadingGoals.value = true
            try {
                // groupId는 이미 ViewModel에 선언된 값을 사용합니다.
                val goalList = groupRepository.getGroupGoals(groupId.toString())
                _goals.value = goalList
            } catch (e: Exception) {
                Log.e("AdminDetailVM", "그룹 목표 로드 실패", e)
                // TODO: 에러 상태 관리
            } finally {
                _isLoadingGoals.value = false
            }
        }
    }
}