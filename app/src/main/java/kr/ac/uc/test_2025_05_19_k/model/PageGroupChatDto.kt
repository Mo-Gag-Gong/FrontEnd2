package kr.ac.uc.test_2025_05_19_k.model

// 페이지네이션을 위한 래퍼 클래스
data class PageGroupChatDto(
    val content: List<GroupChatDto>,
    val totalPages: Int,
    val totalElements: Long,
    val last: Boolean,
    val number: Int
)