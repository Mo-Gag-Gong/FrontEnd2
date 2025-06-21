package kr.ac.uc.test_2025_05_19_k.network.api


import kr.ac.uc.test_2025_05_19_k.viewmodel.GoalResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface GoalApiService {
    // 특정 그룹의 목표 리스트 조회
    @GET("/api/groups/{groupId}/goals")
    suspend fun getGoals(
        @Path("groupId") groupId: Long
    ): Response<List<GoalResponse>>

    @GET("/api/goals/personal")
    suspend fun getMyPersonalGoals(): List<GoalResponse>
}