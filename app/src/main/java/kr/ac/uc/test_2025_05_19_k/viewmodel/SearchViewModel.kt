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
import kr.ac.uc.test_2025_05_19_k.model.StudyGroup
import kr.ac.uc.test_2025_05_19_k.repository.GroupRepository
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    application: Application,
    private val groupRepository: GroupRepository
) : AndroidViewModel(application) {

    private val userPreference = UserPreference(application)

    // 검색 결과 상태
    private val _searchResults = MutableStateFlow<List<StudyGroup>>(emptyList())
    val searchResults: StateFlow<List<StudyGroup>> = _searchResults.asStateFlow()

    // 로딩 및 에러 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // 최근 검색어 상태
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    // --- 그룹 지원 관련 상태 추가 ---
    private val _dialogState = MutableStateFlow(false)
    val dialogState: StateFlow<Boolean> = _dialogState.asStateFlow()

    private val _selectedGroupId = MutableStateFlow<Long?>(null) // 타입을 Long? 으로 변경
    val selectedGroupId: StateFlow<Long?> = _selectedGroupId.asStateFlow()

    private val _applicationStatus = MutableStateFlow<Pair<Boolean, String>?>(null)
    val applicationStatus: StateFlow<Pair<Boolean, String>?> = _applicationStatus.asStateFlow()
    // --- ---

    init {
        loadRecentSearches()
    }

    fun searchGroups(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val region = userPreference.getLocation()
                val result = groupRepository.searchGroups(keyword = query)
                _searchResults.value = result.filter { it.locationName == region }
            } catch (e: Exception) {
                _error.value = "검색 중 오류가 발생했습니다."
                Log.e("SearchViewModel", "Search failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- 그룹 지원 관련 함수 추가 ---
    fun showApplyDialog(groupId: Long) {
        _selectedGroupId.value = groupId
        _dialogState.value = true
    }
    fun dismissDialog() {
        _dialogState.value = false
        _selectedGroupId.value = null
        _applicationStatus.value = null // 다이얼로그 닫을 때 상태 초기화
    }

    fun applyToGroup(groupId: Long) {
        viewModelScope.launch {
            try {
                groupRepository.applyToGroup(groupId)
                _applicationStatus.value = Pair(true, "스터디 그룹에 성공적으로 지원했습니다.")
            } catch (e: Exception) {
                _applicationStatus.value = Pair(false, "지원에 실패했습니다: ${e.message}")
                Log.e("SearchViewModel", "Apply to group failed", e)
            }
        }
    }
    // --- ---

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
}