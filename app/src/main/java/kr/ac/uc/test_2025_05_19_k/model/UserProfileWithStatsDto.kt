package kr.ac.uc.test_2025_05_19_k.model

import com.google.gson.annotations.SerializedName

data class UserProfileWithStatsDto(
    @SerializedName("userId")
    val userId: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("gender")
    val gender: String?,

    @SerializedName("phoneNumber")
    val phoneNumber: String?,

    @SerializedName("birthDate")
    val birthDate: String?,

    @SerializedName("profileImage")
    val profileImage: String?,

    @SerializedName("interests")
    val interests: List<InterestDto>,

    @SerializedName("groupParticipationCount")
    val groupParticipationCount: Int,

    @SerializedName("attendanceRate")
    val attendanceRate: Double,

    @SerializedName("totalMeetings")
    val totalMeetings: Int,

    @SerializedName("statsLastUpdated")
    val statsLastUpdated: String?,

    @SerializedName("isOwnProfile")
    val isOwnProfile: Boolean
)