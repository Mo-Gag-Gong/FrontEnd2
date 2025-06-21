// repository/GoalRepository.kt
package kr.ac.uc.test_2025_05_19_k.repository

import kr.ac.uc.test_2025_05_19_k.network.api.GoalApiService
import kr.ac.uc.test_2025_05_19_k.network.api.GroupApi
import kr.ac.uc.test_2025_05_19_k.viewmodel.GoalResponse
import javax.inject.Inject

class GoalRepository @Inject constructor(
    private val goalApi: GoalApiService,
    private val groupApi: GroupApi
) {
    // ✅ 자신이 참여한 그룹 ID 목록 가져오기
    suspend fun getJoinedGroupIds(): List<Long> {
        return try {
            groupApi.getMyOwnedGroups().map { it.groupId }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ✅ 특정 그룹의 목표 목록 가져오기
    suspend fun getGoalsByGroup(groupId: Long): List<GoalResponse> {
        return try {
            val response = goalApi.getGoals(groupId)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMyPersonalGoals(): List<GoalResponse> {
        return goalApi.getMyPersonalGoals()
    }


}
