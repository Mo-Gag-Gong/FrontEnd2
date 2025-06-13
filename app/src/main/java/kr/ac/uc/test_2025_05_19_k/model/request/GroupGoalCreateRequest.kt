package kr.ac.uc.test_2025_05_19_k.model.request

data class GroupGoalCreateRequest(
    val title: String,
    val pointValue: Int = 0,
    val startDate: String,
    val endDate: String,
    val details: List<String>
)