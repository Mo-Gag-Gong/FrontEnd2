package kr.ac.uc.test_2025_05_19_k.model

data class GroupGoalDto(
    val goalId: String,
    val title: String?,
    val creatorName: String?,
    val startDate: String?,
    val endDate: String?,
    val status: String?,
    val details: List<GoalDetailDto>
)