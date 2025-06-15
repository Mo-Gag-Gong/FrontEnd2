// mo-gag-gong/frontend/frontend-dev-hj/app/src/main/java/kr/ac/uc/test_2025_05_19_k/viewmodel/HomeViewModel.kt
package kr.ac.uc.test_2025_05_19_k.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.data.local.UserPreference
import kr.ac.uc.test_2025_05_19_k.model.Interest
import kr.ac.uc.test_2025_05_19_k.model.PageStudyGroupDto
import kr.ac.uc.test_2025_05_19_k.model.StudyGroup
import kr.ac.uc.test_2025_05_19_k.repository.GroupRepository
import kr.ac.uc.test_2025_05_19_k.repository.InterestRepository
import kr.ac.uc.test_2025_05_19_k.util.AppEvents
import kr.ac.uc.test_2025_05_19_k.util.RefreshEvent
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val groupRepository: GroupRepository,
    private val interestRepository: InterestRepository,
    private val appEvents: AppEvents
) : AndroidViewModel(application) {

    enum class DialogState { HIDDEN, SUCCESS, PENDING }

    private val userPreference = UserPreference(application)
    private val joinedGroupIds = MutableStateFlow<Set<Long>>(emptySet())

    private val _region = MutableStateFlow("")
    val region: StateFlow<String> = _region.asStateFlow()

    private val _interests = MutableStateFlow<List<Interest>>(emptyList())
    val interests: StateFlow<List<Interest>> = _interests.asStateFlow()

    private val _groupList = MutableStateFlow<List<StudyGroup>>(emptyList())
    val groupList: StateFlow<List<StudyGroup>> = _groupList.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedInterest = MutableStateFlow<String?>(null)
    val selectedInterest: StateFlow<String?> = _selectedInterest.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    private val _isLoadingInitial = MutableStateFlow(false)
    val isLoadingInitial: StateFlow<Boolean> = _isLoadingInitial.asStateFlow()

    private val _isLoadingNextPage = MutableStateFlow(false)
    val isLoadingNextPage: StateFlow<Boolean> = _isLoadingNextPage.asStateFlow()

    private val _isLastPage = MutableStateFlow(false)
    val isLastPage: StateFlow<Boolean> = _isLastPage.asStateFlow()

    private val _dialogState = MutableStateFlow(DialogState.HIDDEN)
    val dialogState: StateFlow<DialogState> = _dialogState.asStateFlow()

    private val pageSize = 10

    init {
        initUser()
        loadRecentSearches()
        observeAppEvents()
        fetchJoinedGroupIds()
    }

    fun dismissDialog() {
        _dialogState.value = DialogState.HIDDEN
    }

    fun initUser() {
        viewModelScope.launch {
            try {
                val location = userPreference.getLocation()
                _region.value = location
                Log.d("HomeViewModel", "사용자 지역 정보 로드: ${_region.value}")
                fetchInterests()
                loadGroupsFirstPage()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "초기 데이터 불러오기 실패: ${e.message}")
                _groupList.value = emptyList()
                _interests.value = emptyList()
            }
        }
    }
    private fun observeAppEvents() {
        viewModelScope.launch {
            appEvents.events.collect { event ->
                when (event) {
                    is RefreshEvent.LocationUpdated -> {
                        Log.d("HomeViewModel", "위치 변경 이벤트 수신. 데이터 재초기화.")
                        // 사용자 정보와 그룹 목록을 모두 새로 불러오는 initUser() 호출
                        initUser()
                    }
                }
            }
        }
    }

    private fun fetchJoinedGroupIds() {
        viewModelScope.launch {
            try {
                val joinedGroups = groupRepository.getMyJoinedGroups()
                joinedGroupIds.value = joinedGroups?.map { it.groupId }?.toSet() ?: emptySet()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "가입한 그룹 목록 로드 실패", e)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        // 이 함수는 SearchScreen에서 검색어 입력 시 사용될 수 있으며,
        // 실제 검색 실행은 SearchScreen의 onSearch 콜백에서 네비게이션 후
        // SearchResultScreen의 LaunchedEffect에서 fetchSearchResults를 호출합니다.
        // 따라서 이 함수 자체에서 fetchSearchResults를 바로 호출할 필요는 없을 수 있습니다.
        // 만약 실시간 검색 결과 업데이트가 필요하다면 여기서 호출할 수 있습니다.
        // 현재 구조에서는 SearchResultScreen에서 검색어를 받아 로드합니다.
    }

    // fetchSearchResults를 public으로 변경
    fun fetchSearchResults(query: String) {
        if (query.isBlank()) {
            // 검색어가 비어있으면 홈 화면의 첫 페이지를 로드하거나, 빈 목록을 표시
            loadGroupsFirstPage()
            return
        }
        val currentRegion = _region.value
        Log.d("HomeViewModel", "fetchSearchResults 호출 - Region: $currentRegion, Query: $query")
        _isLoadingInitial.value = true // 검색도 초기 로딩으로 간주
        _groupList.value = emptyList() // 이전 목록 초기화 (홈 또는 이전 검색결과)
        _isLastPage.value = true // 검색 결과는 페이지네이션을 현재 지원하지 않으므로, 일단 마지막 페이지로 처리
        viewModelScope.launch {
            try {
                val searchedGroups = groupRepository.searchGroups(keyword = query)
                // API_Final.md에 따르면 searchGroups는 지역 필터링을 지원하지 않음. 클라이언트에서 필터링.
                val regionFilteredGroups = searchedGroups.filter { it.locationName == currentRegion }
                _groupList.value = regionFilteredGroups
                Log.d("HomeViewModel", "검색 결과 업데이트: ${regionFilteredGroups.size}개")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "검색 결과 가져오기 실패: ${e.message}", e)
                _groupList.value = emptyList()
            } finally {
                _isLoadingInitial.value = false
            }
        }
    }


    fun onInterestClick(interestName: String?) {
        _selectedInterest.value = interestName
        Log.d("HomeViewModel", "관심사 선택 변경: $interestName. 그룹 목록 첫 페이지부터 새로고침.")
        loadGroupsFirstPage()
    }

    private fun fetchInterests() {
        viewModelScope.launch {
            try {
                val allInterests = interestRepository.getAllInterests()
                _interests.value = allInterests
                Log.d("HomeViewModel", "관심사 목록 로드 성공: ${allInterests.size}개")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "관심사 목록 로드 실패: ${e.message}")
                _interests.value = emptyList()
            }
        }
    }

    fun loadGroupsFirstPage() {
        _currentPage.value = 0
        _isLastPage.value = false
        _groupList.value = emptyList()
        _isLoadingInitial.value = true
        fetchGroupPage(_currentPage.value)
    }

    fun loadNextGroupPage() {
        if (_isLoadingNextPage.value || _isLastPage.value || _isLoadingInitial.value) {
            Log.d("HomeViewModel", "다음 페이지 로드 조건 미충족: isLoadingNextPage=${_isLoadingNextPage.value}, isLastPage=${_isLastPage.value}, isLoadingInitial=${_isLoadingInitial.value}")
            return
        }
        Log.d("HomeViewModel", "loadNextGroupPage 호출됨. 다음 페이지: ${_currentPage.value + 1}")
        _isLoadingNextPage.value = true
        fetchGroupPage(_currentPage.value + 1)
    }

    private fun fetchGroupPage(pageToLoad: Int) {
        val currentRegion = _region.value
        val currentInterest = _selectedInterest.value

        Log.d("HomeViewModel", "fetchGroupPage 시작 - Page: $pageToLoad, Region: $currentRegion, Interest: $currentInterest")

        viewModelScope.launch {
            try {
                val pageResultDto: PageStudyGroupDto = groupRepository.getGroups(
                    region = currentRegion,
                    interest = currentInterest,
                    keyword = null,
                    page = pageToLoad,
                    size = pageSize
                )
                Log.d("HomeViewModel", "GetGroups API 결과 수신 (Page $pageToLoad): ${pageResultDto.content.size}개")

                // [수정] 1. 서버에서 받은 StudyGroupDto를 -> isMember를 포함하는 StudyGroup 모델로 변환
                val studyGroups = pageResultDto.content.map { dto ->
                    StudyGroup(
                        groupId = dto.groupId,
                        creatorId = dto.creatorId,
                        creatorName = dto.creatorName,
                        title = dto.title,
                        interestName = dto.interestName,
                        description = dto.description,
                        locationName = dto.locationName,
                        startDate = dto.startDate,
                        endDate = dto.endDate,
                        maxMembers = dto.maxMembers,
                        currentMembers = dto.currentMembers,
                        requirements = dto.requirements,
                        isActive = dto.isActive,
                        createdAt = dto.createdAt,
                        updatedAt = dto.updatedAt,
                        isMember = joinedGroupIds.value.contains(dto.groupId) // 가입 여부 확인
                    )
                }

                // [수정] 2. 변환된 studyGroupsWithMembership 리스트를 가지고 기존 필터링 로직 수행
                var processedContent = studyGroups

                // 클라이언트 측 지역 필터링
                processedContent = processedContent.filter { it.locationName == currentRegion }
                if (currentInterest != null) {
                    processedContent = processedContent.filter { it.interestName == currentInterest }
                }

                // [수정] 3. 최종적으로 처리된 목록을 _groupList에 업데이트
                if (pageToLoad == 0) {
                    _groupList.value = processedContent
                } else {
                    _groupList.value = _groupList.value + processedContent
                }

                _isLastPage.value = pageResultDto.last
                _currentPage.value = pageToLoad // [개선] API 응답의 number 대신 요청한 페이지 번호로 설정

                Log.d("HomeViewModel", "그룹 목록 업데이트 후: 총 ${_groupList.value.size}개")

            } catch (e: Exception) {
                Log.e("HomeViewModel", "그룹 페이지($pageToLoad) 로드 실패: ${e.message}", e)
            } finally {
                if (pageToLoad == 0) _isLoadingInitial.value = false
                _isLoadingNextPage.value = false
            }
        }
    }

    fun loadRecentSearches() {
        _recentSearches.value = userPreference.getRecentSearches().sortedByDescending { it }
    }

    fun addRecentSearch(query: String) {
        if (query.isNotBlank()) {
            userPreference.addRecentSearch(query)
            loadRecentSearches()
        }
    }

    fun removeRecentSearch(query: String) {
        userPreference.removeRecentSearch(query)
        loadRecentSearches()
    }

    fun clearAllRecentSearches() {
        userPreference.clearRecentSearches()
        loadRecentSearches()
    }
    
    suspend fun applyToGroup(groupId: Long) {
        try {
            groupRepository.applyToGroup(groupId)
            // API 호출이 성공하면 (Exception이 발생하지 않으면) 성공 상태로 변경
            _dialogState.value = DialogState.SUCCESS
        } catch (e: Exception) {
            // HttpException이고 코드가 400이면, 이미 신청했거나 다른 문제 발생
            if (e is HttpException && e.code() == 400) {
                // 서버의 에러 메시지를 파싱하여 더 정확하게 분기할 수 있으나,
                // 여기서는 400 에러를 '이미 신청/대기 중'으로 간주합니다.
                _dialogState.value = DialogState.PENDING
            } else {
                // 그 외 네트워크 오류 등
                Log.e("HomeViewModel", "Failed to apply to group", e)
                throw e // UI단에서 추가 처리가 필요하면 에러를 다시 던짐
            }
        }
    }
}