package kr.ac.uc.test_2025_05_19_k.network

import kr.ac.uc.test_2025_05_19_k.model.Interest
import kr.ac.uc.test_2025_05_19_k.model.InterestDto
import kr.ac.uc.test_2025_05_19_k.model.OnboardingStatusResponse
import kr.ac.uc.test_2025_05_19_k.model.ProfileRequest
import kr.ac.uc.test_2025_05_19_k.model.ProfileUpdateRequest
import kr.ac.uc.test_2025_05_19_k.model.RefreshTokenRequest
import kr.ac.uc.test_2025_05_19_k.model.UserProfileResponse
import kr.ac.uc.test_2025_05_19_k.model.auth.TokenResponse


import retrofit2.http.*
import okhttp3.ResponseBody
import retrofit2.Response



// ✅ 서버 API를 호출하는 Retrofit 인터페이스
interface ApiService {

    // 🔹 [GET] 사용자 자신의 프로필 조회
    @GET("/api/users/profile")
    suspend fun getMyProfile(): UserProfileResponse

    // 🔹 [PUT] 사용자 프로필 업데이트
    @PUT("/api/users/profile")
    suspend fun updateProfile(
        @Body request: ProfileUpdateRequest
    ): Response<Unit>


    // 🔹 [GET] 온보딩 완료 여부 확인
    @GET("/api/users/onboarding-status")
    suspend fun getOnboardingStatus(): OnboardingStatusResponse

    // 🔹 [GET] 내가 선택한 관심사 목록 조회
    @GET("/api/users/my-interests")
    suspend fun getMyInterests(): List<Interest>

    // 🔹 [GET] 전체 관심사 목록 조회
    @GET("/api/users/interests")
    suspend fun getAllInterests(): List<InterestDto>


    // 🔹 [POST] 관심사 추가
    @POST("/api/users/interests/{interestId}")
    suspend fun addInterest(
        @Path("interestId") id: Long
    ): Response<Unit>

    // 🔹 관심사 삭제 API - interestId를 Long으로 처리
    @DELETE("/api/users/interests/{interestId}")
    suspend fun deleteInterest(
        @Path("interestId") id: Long
    ): Response<Unit>


    // 🔹 [GET] 현재 로그인한 사용자 정보 (OAuth 토큰 기반)
    @GET("/api/auth/me")
    suspend fun getCurrentUser(): UserProfileResponse

    // 🔹 [POST] 토큰 리프레시 요청
    @POST("/api/auth/token/refresh")
    suspend fun refreshToken(
        @Body refreshTokenRequest: RefreshTokenRequest
    ): Response<TokenResponse>


    @PUT("api/users/{userId}/profile")
    suspend fun updateUserProfile(
        @Path("userId") userId: Long,
        @Body profileRequest: ProfileRequest
    ): Response<Unit>

    @PUT("/api/users/location")
    suspend fun updateLocation(
        @Query("userId") userId: Long,
        @Body body: Map<String, String>
    ): Response<Unit>


}
