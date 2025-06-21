package kr.ac.uc.test_2025_05_19_k.network.api

import kr.ac.uc.test_2025_05_19_k.model.UserProfileResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApiService {
    @GET("/api/users/{userId}/profile")
    suspend fun getUserProfile(@Path("userId") userId: Long): Response<UserProfileResponse>
}