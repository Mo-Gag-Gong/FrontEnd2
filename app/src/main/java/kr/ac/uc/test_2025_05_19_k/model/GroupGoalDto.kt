package kr.ac.uc.test_2025_05_19_k.model

import com.google.gson.annotations.SerializedName

data class GroupGoalDto(
    @SerializedName("goalId")
    val goalId: Long,
    @SerializedName("groupId")
    val groupId: Long,
    @SerializedName("creatorId")
    val creatorId: Long,
    @SerializedName("creatorName")
    val creatorName: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("pointValue")
    val pointValue: Int,
    @SerializedName("startDate")
    val startDate: String?,
    @SerializedName("endDate")
    val endDate: String?,
    @SerializedName("details")
    val details: List<GoalDetailDto>,
    @SerializedName("completedCount")
    val completedCount: Int,
    @SerializedName("totalCount")
    val totalCount: Int
)
