// GoalViewModel.kt
package kr.ac.uc.test_2025_05_19_k.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.repository.GoalRepository
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val repository: GoalRepository
) : ViewModel() {

    // ✅ 서버에서 받은 그룹 목표들 날짜별로 저장
    private val _goalMap = MutableStateFlow<Map<LocalDate, List<GoalResponse>>>(emptyMap())
    val goalMap: StateFlow<Map<LocalDate, List<GoalResponse>>> = _goalMap

    // ✅ 서버로부터 그룹 목표 리스트 로드
    fun loadGoalsFromMyGroups() {
        viewModelScope.launch {
            val groupIds = repository.getJoinedGroupIds()
            val allGoals = mutableListOf<GoalResponse>()

            for (groupId in groupIds) {
                val goals = repository.getGoalsByGroup(groupId)
                allGoals += goals
            }

            val dateMap = mutableMapOf<LocalDate, MutableList<GoalResponse>>()
            for (goal in allGoals) {
                val start = LocalDate.parse(goal.startDate)
                val end = LocalDate.parse(goal.endDate)
                var date = start
                while (!date.isAfter(end)) {
                    dateMap.getOrPut(date) { mutableListOf() }.add(goal)
                    date = date.plusDays(1)
                }
            }

            _goalMap.value = dateMap
        }
    }
}
