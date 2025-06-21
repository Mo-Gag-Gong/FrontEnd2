package kr.ac.uc.test_2025_05_19_k.model

data class InterestResponse(
    val interestName: String,
    val interestId: Long = -1L  // 선택적 ID, 필요 없는 경우 기본값 -1
)
