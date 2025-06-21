// app/src/main/java/kr/ac/uc/test_2025_05_19_k/model/UserProfileResponse.kt
package kr.ac.uc.test_2025_05_19_k.model

/**
 * 사용자 프로필 조회 응답 모델
 * 서버 응답 필드에 맞게 수정/확장 필요
 */
data class UserProfileResponse(
    val userId: Long = -1L,                      // 기본값으로 선택적
    val name: String,
    val email: String = "",                      // 선택적: 사용 안 하면 기본값
    val gender: String? = null,                  // null 허용
    val phoneNumber: String? = null,
    val birthYear: Int? = null,                  // Int 또는 String → 통일 필요
    val profileImage: String? = null,
    val interests: List<InterestResponse> = emptyList(),  // 서버 DTO 기준 맞춤
    val locationName: String?,
    val groupParticipationCount: Int = 0,
    val attendanceRate: Int = 0,
    val totalMeetings: Int = 0,
    val statsLastUpdated: String? = null,        // 통계 갱신일자 (선택적)
    val isOwnProfile: Boolean = true             // 내 프로필 여부, 기본값 true
)

