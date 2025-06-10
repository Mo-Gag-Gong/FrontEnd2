// mo-gag-gong/frontend/frontend-dev-hj/app/src/main/java/kr/ac/uc/test_2025_05_19_k/repository/GroupRepository.kt
package kr.ac.uc.test_2025_05_19_k.repository

import kr.ac.uc.test_2025_05_19_k.model.GroupMemberDto
import kr.ac.uc.test_2025_05_19_k.model.GroupNoticeDto
import kr.ac.uc.test_2025_05_19_k.model.PageGroupNoticeDto
import kr.ac.uc.test_2025_05_19_k.model.StudyGroup // StudyGroup은 이미 임포트 되어 있을 것입니다.
import kr.ac.uc.test_2025_05_19_k.model.StudyGroupDetail
import kr.ac.uc.test_2025_05_19_k.network.api.GroupApi
import kr.ac.uc.test_2025_05_19_k.model.request.GroupCreateRequest
import kr.ac.uc.test_2025_05_19_k.model.PageStudyGroupDto // PageStudyGroupDto 임포트
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

    suspend fun getGroupMembers(groupId: Long): List<GroupMemberDto> {
        return groupApi.getGroupMembers(groupId)
    }

    suspend fun getPendingMembers(groupId: Long): List<GroupMemberDto> {
        return groupApi.getPendingMembers(groupId)
    }
    suspend fun approveMember(groupId: Long, userId: Long): Response<Void> {
        return groupApi.approveMember(groupId, userId)
    }

    suspend fun rejectMember(groupId: Long, userId: Long): Response<Void> {
        return groupApi.rejectMember(groupId, userId)
    }
}