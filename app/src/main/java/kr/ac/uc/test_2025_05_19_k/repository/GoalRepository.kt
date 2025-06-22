// repository/GoalRepository.kt
package kr.ac.uc.test_2025_05_19_k.repository

import kr.ac.uc.test_2025_05_19_k.network.api.GoalApiService
import kr.ac.uc.test_2025_05_19_k.network.api.GroupApi
import kr.ac.uc.test_2025_05_19_k.viewmodel.GoalResponse
import javax.inject.Inject
import kr.ac.uc.test_2025_05_19_k.model.StudyGroup

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

    suspend fun getJoinedGroups(): List<StudyGroup> {
        return try {
            // 소유한 그룹이 아닌 참여한 그룹을 가져오도록 API 호출 수정
            groupApi.getMyJoinedGroups()
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

    suspend fun getAllMyRelatedGroups(): List<StudyGroup> {
        return try {
            val ownedGroups = groupApi.getMyOwnedGroups()
            val joinedGroups = groupApi.getMyJoinedGroups()
            // 두 목록을 합친 후 groupId를 기준으로 중복 제거
            (ownedGroups + joinedGroups).distinctBy { it.groupId }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMyPersonalGoals(): List<GoalResponse> {
        return goalApi.getMyPersonalGoals()
    }

    suspend fun getGroupName(groupId: Long): String {
        return try {
            groupApi.getGroupDetail(groupId).title
        } catch (e: Exception) {
            "이름없는 그룹" // 실패 시 기본값 반환
        }
    }


}
