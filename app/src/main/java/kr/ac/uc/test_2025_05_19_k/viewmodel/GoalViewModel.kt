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
    /**
     * 연속된 날짜의 동일 제목 일정을 병합
     */
    fun mergeContinuousPersonalGoals(goals: List<PersonalGoal>): List<MergedGoal> {
        if (goals.isEmpty()) return emptyList()

        val sorted = goals.sortedBy { it.date }
        val merged = mutableListOf<MergedGoal>()

        var startDate = LocalDate.parse(sorted[0].date)
        var endDate = startDate
        var currentTitle = sorted[0].title

        for (i in 1 until sorted.size) {
            val current = sorted[i]
            val currentDate = LocalDate.parse(current.date)

            if (currentDate == endDate.plusDays(1) && current.title == currentTitle) {
                // 연속된 일정이며 제목도 같으면 병합 연장
                endDate = currentDate
            } else {
                // 이전 병합 종료
                merged.add(
                    MergedGoal(
                        startDate.toString(),
                        endDate.toString(),
                        currentTitle
                    )
                )
                // 새 병합 시작
                startDate = currentDate
                endDate = currentDate
                currentTitle = current.title
            }
        }

        // 마지막 일정 병합 처리
        merged.add(
            MergedGoal(
                startDate.toString(),
                endDate.toString(),
                currentTitle
            )
        )

        return merged
    }
}


// 일정 병합에 사용되는 데이터 클래스
data class PersonalGoal(val date: String, val title: String)

// 연속된 일정 결과 구조
data class MergedGoal(val startDate: String, val endDate: String, val title: String)

// ViewModel 내에 추가
fun mergeContinuousPersonalGoals(goals: List<PersonalGoal>): List<MergedGoal> {
    if (goals.isEmpty()) return emptyList()

    val sorted = goals.sortedWith(compareBy({ it.title }, { it.date }))
    val result = mutableListOf<MergedGoal>()

    var start = LocalDate.parse(sorted[0].date)
    var end = start
    var currentTitle = sorted[0].title

    for (i in 1 until sorted.size) {
        val g = sorted[i]
        val date = LocalDate.parse(g.date)

        if (g.title == currentTitle && date == end.plusDays(1)) {
            end = date
        } else {
            result.add(MergedGoal(start.toString(), end.toString(), currentTitle))
            start = date
            end = date
            currentTitle = g.title
        }
    }

    result.add(MergedGoal(start.toString(), end.toString(), currentTitle))
    return result
}

