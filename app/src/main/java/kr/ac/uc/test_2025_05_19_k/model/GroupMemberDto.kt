// app/src/main/java/kr/ac/uc/test_2025_05_19_k/model/GroupMemberDto.kt
package kr.ac.uc.test_2025_05_19_k.model

data class GroupMemberDto(
    val membershipId: Long,
    val userId: Long,
    val userName: String,
    val profileImage: String?, // Nullable일 수 있음
    val joinDate: String, // "yyyy-MM-dd" 형식의 문자열 또는 적절한 날짜 타입
    val status: String // "PENDING", "ACTIVE" 등
)