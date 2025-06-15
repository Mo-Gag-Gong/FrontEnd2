// mo-gag-gong/frontend2/FrontEnd2-34c64adbf8218e74ead67775384f12f5a0320126/app/src/main/java/kr/ac/uc/test_2025_05_19_k/repository/GroupRepository.kt
package kr.ac.uc.test_2025_05_19_k.repository

import kr.ac.uc.test_2025_05_19_k.model.GroupChatDto
import kr.ac.uc.test_2025_05_19_k.model.GroupGoalDto
import kr.ac.uc.test_2025_05_19_k.model.GroupMemberDto
import kr.ac.uc.test_2025_05_19_k.model.GroupNoticeDto
import kr.ac.uc.test_2025_05_19_k.model.PageGroupChatDto
import kr.ac.uc.test_2025_05_19_k.model.PageGroupNoticeDto
import kr.ac.uc.test_2025_05_19_k.model.StudyGroup // StudyGroup은 이미 임포트 되어 있을 것입니다.
import kr.ac.uc.test_2025_05_19_k.model.StudyGroupDetail
import kr.ac.uc.test_2025_05_19_k.network.api.GroupApi
import kr.ac.uc.test_2025_05_19_k.model.request.GroupCreateRequest
import kr.ac.uc.test_2025_05_19_k.model.PageStudyGroupDto // PageStudyGroupDto 임포트
import kr.ac.uc.test_2025_05_19_k.model.request.GroupChatCreateRequest
import kr.ac.uc.test_2025_05_19_k.model.request.GroupGoalCreateRequest
import kr.ac.uc.test_2025_05_19_k.model.request.GroupNoticeCreateRequest
import retrofit2.Response
import javax.inject.Inject

class GroupRepository @Inject constructor(
    private val groupApi: GroupApi
) {
    // 반환 타입을 PageStudyGroupDto로 변경하고, page와 size는 필수 인자로 받도록 함
    suspend fun getGroups(region: String, keyword: String?, interest: String?, page: Int, size: Int): PageStudyGroupDto {
        return groupApi.getGroups(region, keyword, interest, page, size)
    }

    suspend fun getGroupDetail(groupId: Long): StudyGroupDetail {
        return groupApi.getGroupDetail(groupId)
    }

    suspend fun applyToGroup(groupId: Long) {
        groupApi.applyToGroup(groupId)
    }

    suspend fun createGroup(request: GroupCreateRequest) {
        groupApi.createGroup(request)
    }

    // 검색 API는 페이지네이션을 지원하지 않으므로, 반환 타입은 List<StudyGroup> 유지
    suspend fun searchGroups(keyword: String, page: Int? = 0, size: Int? = 10): List<StudyGroup> {
        val pageResult: PageStudyGroupDto = groupApi.searchGroups(keyword, page, size)
        return pageResult.content
    }

    suspend fun getMyJoinedGroups(): List<StudyGroup> {
        return groupApi.getMyJoinedGroups()
    }

    /**
     * 현재 사용자가 생성한 스터디 그룹 목록을 가져옵니다.
     */
    suspend fun getMyOwnedGroups(): List<StudyGroup> {
        return groupApi.getMyOwnedGroups()
    }

    suspend fun updateGroup(groupId: Long, request: GroupCreateRequest): Response<StudyGroupDetail> {
        return groupApi.updateGroup(groupId, request)
    }
    suspend fun getGroupNotices(groupId: Long, page: Int, size: Int): PageGroupNoticeDto {
        return groupApi.getGroupNotices(groupId, page, size)
    }

    suspend fun createNotice(groupId: Long, request: GroupNoticeCreateRequest): GroupNoticeDto {
        return groupApi.createNotice(groupId, request)
    }
    suspend fun deleteNotice(groupId: Long, noticeId: Long): Response<Void> {
        return groupApi.deleteNotice(groupId, noticeId)
    }
    suspend fun updateNotice(groupId: Long, noticeId: Long, request: GroupNoticeCreateRequest): GroupNoticeDto {
        return groupApi.updateNotice(groupId, noticeId, request)
    }

    suspend fun getGroupMembers(groupId: Long): Result<List<GroupMemberDto>> = try {
        val response = groupApi.getGroupMembers(groupId)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to fetch group members"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun kickMember(groupId: Long, userId: Long): Result<Unit> = try {
        val response = groupApi.kickMember(groupId, userId)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to kick member"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getPendingMembers(groupId: Long): Result<List<GroupMemberDto>> = try {
        val response = groupApi.getPendingMembers(groupId)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to fetch pending members"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun approveMember(groupId: Long, userId: Long): Result<Unit> = try {
        val response = groupApi.approveMember(groupId, userId)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to approve member"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun rejectMember(groupId: Long, userId: Long): Result<Unit> = try {
        val response = groupApi.rejectMember(groupId, userId)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to reject member"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getGroupGoals(groupId: String): List<GroupGoalDto> {
        val response = groupApi.getGroupGoals(groupId)
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw Exception("Failed to get group goals")
        }
    }

    suspend fun createGoal(groupId: String, request: GroupGoalCreateRequest): GroupGoalDto {
        val response = groupApi.createGoal(groupId, request)
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw Exception("Failed to create goal")
        }
    }

    suspend fun getGoalDetails(groupId: String, goalId: String): GroupGoalDto {
        val response = groupApi.getGoalDetails(groupId, goalId)
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw Exception("Failed to get goal details")
        }
    }

    suspend fun updateGoal(groupId: String, goalId: String, request: GroupGoalCreateRequest): GroupGoalDto {
        val response = groupApi.updateGoal(groupId, goalId, request)
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw Exception("Failed to update goal")
        }
    }

    suspend fun deleteGoal(groupId: String, goalId: String) {
        groupApi.deleteGoal(groupId, goalId)
    }

    suspend fun toggleGoalDetail(groupId: String, goalId: String, detailId: String) {
        val response = groupApi.toggleGoalDetail(groupId, goalId, detailId)
        if (!response.isSuccessful) {
            throw Exception("Failed to toggle goal detail")
        }
    }

    suspend fun getGroupChats(groupId: Long, page: Int): PageGroupChatDto {
        val response = groupApi.getGroupChats(groupId, page)
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw Exception("Failed to get chat messages")
        }
    }

    suspend fun sendChatMessage(groupId: Long, request: GroupChatCreateRequest): GroupChatDto {
        val response = groupApi.sendChatMessage(groupId, request)
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw Exception("Failed to send message")
        }
    }

    suspend fun leaveGroup(groupId: Long) {
        val response = groupApi.leaveGroup(groupId)
        if (!response.isSuccessful) {
            throw Exception("Failed to leave group: ${response.code()}")
        }
    }
}