package kr.ac.uc.test_2025_05_19_k.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.model.StudyGroupDetail
import kr.ac.uc.test_2025_05_19_k.repository.GroupRepository
import javax.inject.Inject
import android.util.Log
import kr.ac.uc.test_2025_05_19_k.model.GroupGoalDto
import kr.ac.uc.test_2025_05_19_k.model.GroupMemberDto
import kr.ac.uc.test_2025_05_19_k.model.GroupNoticeDto
import kr.ac.uc.test_2025_05_19_k.model.GroupChatDto
import kr.ac.uc.test_2025_05_19_k.model.UserProfileWithStatsDto
import kr.ac.uc.test_2025_05_19_k.model.request.GroupChatCreateRequest
import kr.ac.uc.test_2025_05_19_k.repository.TokenManager
import kr.ac.uc.test_2025_05_19_k.repository.UserRepository
import retrofit2.HttpException

// ▼▼▼ [수정] 생성자에 UserRepository 추가 ▼▼▼
@HiltViewModel
class GroupAdminDetailViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _groupMembers = MutableStateFlow<List<GroupMemberDto>>(emptyList())
    val groupMembers: StateFlow<List<GroupMemberDto>> = _groupMembers.asStateFlow()

    val groupId: Long = savedStateHandle.get<Long>("groupId") ?: -1L

    private val _groupDetail = MutableStateFlow<StudyGroupDetail?>(null)
    val groupDetail: StateFlow<StudyGroupDetail?> = _groupDetail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _groupNotices = MutableStateFlow<List<GroupNoticeDto>>(emptyList())
    val groupNotices: StateFlow<List<GroupNoticeDto>> = _groupNotices.asStateFlow()

    private val _isLoadingNotices = MutableStateFlow(false)
    val isLoadingNotices: StateFlow<Boolean> = _isLoadingNotices.asStateFlow()

    private var currentNoticePage = 0
    private val noticePageSize = 20
    private var isLastNoticePage = false

    private val _showDeleteConfirmDialog = MutableStateFlow(false)
    val showDeleteConfirmDialog: StateFlow<Boolean> = _showDeleteConfirmDialog.asStateFlow()

    private var noticeIdToDelete: Long? = null

    private val _goals = MutableStateFlow<List<GroupGoalDto>>(emptyList())
    val goals: StateFlow<List<GroupGoalDto>> = _goals.asStateFlow()

    private val _isLoadingGoals = MutableStateFlow(false)
    val isLoadingGoals: StateFlow<Boolean> = _isLoadingGoals.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<GroupChatDto>>(emptyList())
    val chatMessages: StateFlow<List<GroupChatDto>> = _chatMessages.asStateFlow()

    private val _chatInputText = MutableStateFlow("")
    val chatInputText: StateFlow<String> = _chatInputText.asStateFlow()

    val myUserId: Long? = tokenManager.getUserId()

    private var currentChatPage = 0
    private var isChatLastPage = false

    // ▼▼▼ [추가] 멤버 상세 정보 관련 상태 변수 및 함수들 ▼▼▼
    private val _selectedMemberProfile = MutableStateFlow<UserProfileWithStatsDto?>(null)
    val selectedMemberProfile: StateFlow<UserProfileWithStatsDto?> = _selectedMemberProfile.asStateFlow()

    private val _hasPendingMembers = MutableStateFlow(false)
    val hasPendingMembers: StateFlow<Boolean> = _hasPendingMembers.asStateFlow()

    init {
        if (groupId != -1L) {
            fetchGroupDetails()
            fetchNoticesFirstPage()
            fetchGroupMembers()
        }
    }
    fun onTabSelected(index: Int) {
        _selectedTabIndex.value = index
    }

    private fun fetchGroupDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _groupDetail.value = groupRepository.getGroupDetail(groupId)
            } catch (e: Exception) {
                Log.e("GroupAdminDetailVM", "그룹 상세 정보 로드 실패: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun fetchGroupMembers() {
        viewModelScope.launch {
            groupRepository.getGroupMembers(groupId).onSuccess { members ->
                _groupMembers.value = members.filter { it.status == "ACTIVE" }
            }.onFailure {
                Log.e("GroupAdminDetailVM", "멤버 목록 로드 실패", it)
            }
        }
    }

    fun kickMember(userId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            groupRepository.kickMember(groupId, userId).onSuccess {
                fetchGroupMembers()
                onSuccess()
            }.onFailure {
                Log.e("GroupAdminDetailVM", "멤버 추방 실패", it)
            }
        }
    }

    fun onMemberSelected(userId: Long) {
        viewModelScope.launch {
            _selectedMemberProfile.value = null
            userRepository.getUserProfile(userId)
                .onSuccess { profile ->
                    _selectedMemberProfile.value = profile
                }
                .onFailure { e ->
                    Log.e("GroupAdminDetailVM", "Failed to get member profile", e)
                }
        }
    }

    fun clearSelectedMember() {
        _selectedMemberProfile.value = null
    }

    fun checkPendingMembers() {
        viewModelScope.launch {
            groupRepository.getPendingMembers(groupId)
                .onSuccess { pendingList ->
                    _hasPendingMembers.value = pendingList.isNotEmpty()
                }
                .onFailure {
                    _hasPendingMembers.value = false
                    Log.e("GroupAdminDetailVM", "Failed to check pending members", it)
                }
        }
    }

    // --- 이하 다른 함수들은 기존과 동일 ---
    fun fetchNoticesFirstPage() {
        if (groupId == -1L) return
        currentNoticePage = 0
        isLastNoticePage = false
        _groupNotices.value = emptyList()
        fetchNoticesPage(currentNoticePage)
    }

    private fun fetchNoticesPage(page: Int) {
        viewModelScope.launch {
            _isLoadingNotices.value = true
            try {
                val noticePage = groupRepository.getGroupNotices(groupId, page, noticePageSize)
                _groupNotices.value = noticePage.content
                isLastNoticePage = noticePage.last
                currentNoticePage = noticePage.number
                Log.d("GroupAdminDetailVM", "공지사항 로드 성공: ${noticePage.content.size}개")
            } catch (e: Exception) {
                Log.e("GroupAdminDetailVM", "공지사항 로드 실패: ${e.message}", e)
            } finally {
                _isLoadingNotices.value = false
            }
        }
    }

    fun onOpenDeleteDialog(noticeId: Long) {
        noticeIdToDelete = noticeId
        _showDeleteConfirmDialog.value = true
    }

    fun onDismissDeleteDialog() {
        noticeIdToDelete = null
        _showDeleteConfirmDialog.value = false
    }

    fun deleteNotice(onError: (String) -> Unit) {
        val noticeId = noticeIdToDelete
        if (noticeId == null) {
            onError("삭제할 공지사항이 선택되지 않았습니다.")
            onDismissDeleteDialog()
            return
        }

        viewModelScope.launch {
            try {
                val response = groupRepository.deleteNotice(groupId, noticeId)
                if (response.isSuccessful) {
                    Log.d("GroupAdminDetailVM", "공지사항 삭제 성공 (ID: $noticeId)")
                    fetchNoticesFirstPage()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "알 수 없는 서버 오류"
                    Log.e("GroupAdminDetailVM", "공지사항 삭제 API 실패: ${response.code()} - $errorBody")
                    onError("공지사항 삭제에 실패했습니다.")
                }
            } catch (e: Exception) {
                Log.e("GroupAdminDetailVM", "공지사항 삭제 중 예외 발생: ${e.message}", e)
                onError("오류가 발생하여 공지사항을 삭제하지 못했습니다.")
            } finally {
                onDismissDeleteDialog()
            }
        }
    }

    fun fetchGroupGoals(forceRefresh: Boolean = false) {
        if (!forceRefresh && (_isLoadingGoals.value || _goals.value.isNotEmpty())) return

        viewModelScope.launch {
            _isLoadingGoals.value = true
            try {
                val goalList = groupRepository.getGroupGoals(groupId.toString())
                _goals.value = goalList
            } catch (e: Exception) {
                Log.e("AdminDetailVM", "그룹 목표 로드 실패", e)
            } finally {
                _isLoadingGoals.value = false
            }
        }
    }

    fun fetchInitialChats() {
        if (_chatMessages.value.isNotEmpty()) return
        currentChatPage = 0
        viewModelScope.launch {
            try {
                val chatPage = groupRepository.getGroupChats(groupId, currentChatPage)
                _chatMessages.value = chatPage.content.reversed()
                isChatLastPage = chatPage.last
            } catch (e: Exception) {
                Log.e("AdminDetailVM", "채팅 로드 실패", e)
            }
        }
    }

    fun onChatInputChanged(text: String) {
        _chatInputText.value = text
    }

    fun sendChatMessage() {
        if (_chatInputText.value.isBlank()) return

        val request = GroupChatCreateRequest(message = _chatInputText.value)
        _chatInputText.value = ""

        viewModelScope.launch {
            try {
                groupRepository.sendChatMessage(groupId, request)
                fetchInitialChats()
            } catch (e: Exception) {
                Log.e("AdminDetailVM", "메시지 전송 실패", e)
            }
        }
    }
}