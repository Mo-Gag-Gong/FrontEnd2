// ProfileRepository.kt
package kr.ac.uc.test_2025_05_19_k.repository

import kr.ac.uc.test_2025_05_19_k.model.ProfileUpdateRequest
import kr.ac.uc.test_2025_05_19_k.model.UserProfileResponse
import kr.ac.uc.test_2025_05_19_k.network.ApiService
import kr.ac.uc.test_2025_05_19_k.network.api.UserApi
import kr.ac.uc.test_2025_05_19_k.network.api.UserApiService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // 싱글톤으로 사용하려면 붙이세요 (옵션)
class ProfileRepository @Inject constructor(
    private val apiService: ApiService,
    private val userApi: UserApi,
    private val userApiService: UserApiService
) {
    suspend fun updateProfile(profileRequest: ProfileUpdateRequest): Response<Unit> {
        return apiService.updateProfile(profileRequest)
    }

    suspend fun saveUserInterests(interestIds: List<Long>): Result<Unit> {
        return try {
            // 실제 서버 API 호출 (예: /api/users/interests)
            val response = userApi.updateUserInterests(interestIds)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("서버 응답 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(userId: Long): Result<UserProfileResponse> {
        return try {
            val response = userApiService.getUserProfile(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("응답 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}
