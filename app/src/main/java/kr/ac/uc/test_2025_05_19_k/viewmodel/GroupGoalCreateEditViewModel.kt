package kr.ac.uc.test_2025_05_19_k.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.model.request.GroupGoalCreateRequest
import kr.ac.uc.test_2025_05_19_k.repository.GroupRepository
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kr.ac.uc.test_2025_05_19_k.util.toDate

data class GoalFormState(
    val title: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val details: List<String> = listOf(""),
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showEndDateWarning: Boolean = false,
    val isFormValid: Boolean = false // ▼▼▼ [추가] 폼 유효성 상태 변수
)

@HiltViewModel
class GroupGoalCreateEditViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>("groupId")!!
    private val goalId: String? = savedStateHandle.get<String>("goalId")

    private val _uiState = MutableStateFlow(GoalFormState())
    val uiState = _uiState.asStateFlow()

    init {
        if (goalId != null) {
            loadGoalForEditing()
        }
    }

    fun onEndDateChange(date: String) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val selectedEndDate = date.toDate()
        val shouldShowWarning = selectedEndDate != null && selectedEndDate.before(today)

        _uiState.update {
            val newState = it.copy(endDate = date, showEndDateWarning = shouldShowWarning)
            newState.copy(isFormValid = validateForm(newState))
        }
    }

    private fun validateForm(state: GoalFormState): Boolean {
        return state.title.isNotBlank() &&
                state.startDate.isNotBlank() &&
                state.endDate.isNotBlank()
    }

    private fun loadGoalForEditing() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isEditMode = true) }
            try {
                val goal = groupRepository.getGoalDetails(groupId, goalId!!)
                _uiState.update {
                    val loadedState = it.copy(
                        title = goal.title ?: "",
                        startDate = goal.startDate ?: "",
                        endDate = goal.endDate ?: "",
                        details = goal.details.map { detail -> detail.description ?: "" },
                        isLoading = false
                    )
                    // ▼▼▼ [추가] 로드된 데이터로 유효성 검사 ▼▼▼
                    loadedState.copy(isFormValid = validateForm(loadedState))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "기존 목표 정보를 불러오지 못했습니다.", isLoading = false) }
            }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update {
            val newState = it.copy(title = title)
            newState.copy(isFormValid = validateForm(newState))
        }
    }
    fun onStartDateChange(date: String) {
        _uiState.update {
            val newState = it.copy(startDate = date)
            newState.copy(isFormValid = validateForm(newState))
        }
    }
    fun onDetailChange(index: Int, text: String) {
        val newDetails = _uiState.value.details.toMutableList()
        newDetails[index] = text
        _uiState.update { it.copy(details = newDetails) }
    }

    fun addDetailField() {
        val newDetails = _uiState.value.details + "" // 비어있는 세부 목표 필드 추가
        _uiState.update { it.copy(details = newDetails) }
    }

    fun removeDetailField(index: Int) {
        if (_uiState.value.details.size > 1) { // 최소 1개는 유지
            val newDetails = _uiState.value.details.toMutableList()
            newDetails.removeAt(index)
            _uiState.update { it.copy(details = newDetails) }
        }
    }

    fun saveGoal(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val currentState = _uiState.value

            // ▼▼▼ [추가] 날짜 유효성 검사 로직 ▼▼▼
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val endDate = try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(currentState.endDate)
            } catch (e: Exception) { null }

            if (endDate != null && endDate.before(today)) {
                _uiState.update { it.copy(isLoading = false, error = "종료 날짜는 현재 날짜보다 이전일 수 없습니다.") }
                return@launch
            }

            val request = GroupGoalCreateRequest(
                title = currentState.title,
                pointValue = 0,
                startDate = currentState.startDate,
                endDate = currentState.endDate,
                details = currentState.details.filter { it.isNotBlank() } // 비어있는 내용은 제외
            )

            try {
                if (currentState.isEditMode) {
                    groupRepository.updateGoal(groupId, goalId!!, request)
                } else {
                    groupRepository.createGoal(groupId, request)
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "저장에 실패했습니다.") }
                Log.e("GoalCreateEditVM", "Goal save failed", e)
            }
        }
    }
}


data class GroupGoalCreateEditUiState(
    // ... 기존 변수들
    val error: String? = null,
    val showEndDateWarning: Boolean = false // ▼▼▼ [추가] 종료 날짜 경고 표시 여부
)
