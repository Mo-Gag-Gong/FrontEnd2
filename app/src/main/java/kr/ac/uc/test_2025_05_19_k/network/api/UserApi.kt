package kr.ac.uc.test_2025_05_19_k.network.api

import kr.ac.uc.test_2025_05_19_k.model.*
import kr.ac.uc.test_2025_05_19_k.model.auth.RegisterRequest
import kr.ac.uc.test_2025_05_19_k.model.auth.TokenResponse
import kr.ac.uc.test_2025_05_19_k.model.auth.LoginRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import kr.ac.uc.test_2025_05_19_k.model.UserProfileWithStatsDto // 새로 만든 DTO import
// ... 다른 imports
import retrofit2.Response
import retrofit2.http.PUT

interface UserApi {

    @POST("/api/users/login")
    suspend fun login(
        @Body request: LoginRequest
    ): TokenResponse

    @POST("/api/users/join")
    suspend fun register(
        @Body request: RegisterRequest
    ): Void

    @GET("/api/auth/me")
    suspend fun getMyProfile(): AuthUserProfile

    @GET("api/users/{userId}/profile")
    suspend fun getUserProfile(@Path("userId") userId: Long): Response<UserProfileWithStatsDto>

    @PUT("/api/users/interests")
    suspend fun updateUserInterests(
        @Body interestIds: List<Long>
    ): Response<Unit>




}
