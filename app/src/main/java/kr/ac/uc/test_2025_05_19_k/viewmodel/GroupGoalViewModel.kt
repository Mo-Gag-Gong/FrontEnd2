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
class GroupGoalViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>("groupId")!!

    private val _goals = MutableStateFlow<List<GroupGoalDto>>(emptyList())
    val goals: StateFlow<List<GroupGoalDto>> = _goals.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadGoals()
    }

    fun loadGoals() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val goalList = groupRepository.getGroupGoals(groupId)
                _goals.value = goalList
                Log.d("GroupGoalViewModel", "그룹 목표 ${goalList.size}개 로드 성공")
            } catch (e: Exception) {
                Log.e("GroupGoalViewModel", "그룹 목표 로드 실패", e)
                _error.value = "목표를 불러오는 데 실패했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }
}