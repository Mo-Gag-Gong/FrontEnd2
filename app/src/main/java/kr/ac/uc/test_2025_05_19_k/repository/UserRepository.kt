package kr.ac.uc.test_2025_05_19_k.repository

import kr.ac.uc.test_2025_05_19_k.model.AuthUserProfile
import kr.ac.uc.test_2025_05_19_k.model.UserProfileWithStatsDto
import kr.ac.uc.test_2025_05_19_k.network.api.UserApi
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userApi: UserApi
) {
    suspend fun getMyProfile(): AuthUserProfile {
        return userApi.getMyProfile()
    }
    suspend fun getUserProfile(userId: Long): Result<UserProfileWithStatsDto> = try {
        val response = userApi.getUserProfile(userId)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to fetch user profile with stats"))
        }
    } catch(e: Exception) {
        Result.failure(e)
    }
}
