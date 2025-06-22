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
    private val _goalMap = MutableStateFlow<Map<LocalDate, List<GoalWithGroupName>>>(emptyMap())
    val goalMap: StateFlow<Map<LocalDate, List<GoalWithGroupName>>> = _goalMap

    // ✅ 서버로부터 그룹 목표 리스트 로드
    fun loadGoalsFromMyGroups() {
        viewModelScope.launch {
            val groupIds = repository.getJoinedGroupIds()
            val allGoals = mutableListOf<GoalWithGroupName>()

            for (groupId in groupIds) {
                val goals = repository.getGoalsByGroup(groupId)
                val groupName = repository.getGroupName(groupId) // ✅ 안전하게 이름 가져오기
                for (goal in goals) {
                    allGoals += GoalWithGroupName(goal, groupName)
                }
            }


            val dateMap = mutableMapOf<LocalDate, MutableList<GoalWithGroupName>>()
            for (item in allGoals) {
                val start = LocalDate.parse(item.goal.startDate)
                val end = LocalDate.parse(item.goal.endDate)
                var date = start
                while (!date.isAfter(end)) {
                    dateMap.getOrPut(date) { mutableListOf() }.add(item)
                    date = date.plusDays(1)
                }
            }

            _goalMap.value = dateMap
        }
    }

    /**
     * 연속된 날짜의 동일 제목 + 동일 그룹 일정을 병합
     */
    fun mergeContinuousPersonalGoals(goals: List<PersonalGoal>): List<MergedGoal> {
        if (goals.isEmpty()) return emptyList()

        val sorted = goals.sortedWith(compareBy({ it.groupName }, { it.title }, { it.date }))
        val merged = mutableListOf<MergedGoal>()

        var startDate = LocalDate.parse(sorted[0].date)
        var endDate = startDate
        var currentTitle = sorted[0].title
        var currentGroup = sorted[0].groupName

        for (i in 1 until sorted.size) {
            val current = sorted[i]
            val currentDate = LocalDate.parse(current.date)

            if (currentDate == endDate.plusDays(1) && current.title == currentTitle && current.groupName == currentGroup) {
                endDate = currentDate
            } else {
                merged.add(
                    MergedGoal(
                        startDate.toString(),
                        endDate.toString(),
                        currentTitle,
                        currentGroup
                    )
                )
                startDate = currentDate
                endDate = currentDate
                currentTitle = current.title
                currentGroup = current.groupName
            }
        }

        merged.add(
            MergedGoal(
                startDate.toString(),
                endDate.toString(),
                currentTitle,
                currentGroup
            )
        )

        return merged
    }
}

// 일정 병합에 사용되는 데이터 클래스
data class PersonalGoal(val date: String, val title: String, val groupName: String)

// 연속된 일정 결과 구조
data class MergedGoal(val startDate: String, val endDate: String, val title: String, val groupName: String)

// GoalResponse + groupName을 포함한 구조
data class GoalWithGroupName(
    val goal: GoalResponse,
    val groupName: String
)
