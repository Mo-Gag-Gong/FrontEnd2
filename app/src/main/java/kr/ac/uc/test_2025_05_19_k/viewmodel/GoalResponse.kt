package kr.ac.uc.test_2025_05_19_k.viewmodel

import kr.ac.uc.test_2025_05_19_k.model.GoalDetailResponse

// 목표 전체 모델
data class GoalResponse(
    val goalId: Long,                // 목표 ID
    val groupId: Long,               // 그룹 ID (-1L이면 개인 일정)'
    val groupName: String,
    val creatorId: Long,             // 작성자 ID
    val creatorName: String,         // 작성자 이름
    val title: String,               // 일정 제목
    val pointValue: Int,             // 포인트 (그룹 목표용)
    val startDate: String,           // 시작일자 (형식: yyyy-MM-dd)
    val endDate: String,             // 종료일자 (형식: yyyy-MM-dd)
    val details: List<GoalDetailResponse>, // 상세 목표 리스트 (서브 목표 등)
    val completedCount: Int,         // 완료된 수
    val totalCount: Int              // 전체 항목 수
)


