package kr.ac.uc.test_2025_05_19_k.repository

import kr.ac.uc.test_2025_05_19_k.model.AuthUserProfile
import kr.ac.uc.test_2025_05_19_k.model.UserProfileResponse
import kr.ac.uc.test_2025_05_19_k.model.UserProfileWithStatsDto
import kr.ac.uc.test_2025_05_19_k.network.ApiService
import kr.ac.uc.test_2025_05_19_k.network.api.UserApi
import kr.ac.uc.test_2025_05_19_k.network.api.UserApiService
import retrofit2.Response
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val userApi: UserApi,
    private val userApiService: UserApiService
) {
    // ✅ 1. 로그인된 사용자 자신의 프로필 (토큰 기반)
    suspend fun getMyProfile(): AuthUserProfile {
        return userApi.getMyProfile()
    }

    // ✅ 2. 특정 사용자 ID 기반: 간단한 프로필
    suspend fun getUserProfileSimple(userId: Long): Result<UserProfileResponse> = try {
        val response: Response<UserProfileResponse> = userApiService.getUserProfile(userId)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("UserProfileResponse 실패: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ✅ 3. 특정 사용자 ID 기반: 통계 포함 상세 프로필
    suspend fun getUserProfileWithStats(userId: Long): Result<UserProfileWithStatsDto> = try {
        val response = userApi.getUserProfile(userId)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("UserProfileWithStatsDto 실패: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getUserProfile(userId: Long): Result<UserProfileWithStatsDto> {
        return getUserProfileWithStats(userId)
    }

}
