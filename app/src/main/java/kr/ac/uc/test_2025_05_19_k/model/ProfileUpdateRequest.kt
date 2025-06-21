package kr.ac.uc.test_2025_05_19_k.model


data class ProfileUpdateRequest(
    val name: String,
    val gender: String,
    val phoneNumber: String,
    val locationName: String,
    val birthYear: Int,
    val interestIds: List<Long>
)




