// app/src/main/java/kr/ac/uc/test_2025_05_19_k/viewmodel/GroupEditViewModel.kt
package kr.ac.uc.test_2025_05_19_k.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.model.Interest
import kr.ac.uc.test_2025_05_19_k.model.StudyGroupDetail
import kr.ac.uc.test_2025_05_19_k.model.request.GroupCreateRequest
import kr.ac.uc.test_2025_05_19_k.repository.GroupRepository
import kr.ac.uc.test_2025_05_19_k.repository.InterestRepository
import javax.inject.Inject

sealed class GroupEditEvent {
    data class ShowToast(val message: String) : GroupEditEvent()
    data object NavigateBack : GroupEditEvent()
}

@HiltViewModel
class GroupEditViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val interestRepository: InterestRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val groupId: Long = savedStateHandle.get<Long>("groupId") ?: -1L

    private val _groupDetail = MutableStateFlow<StudyGroupDetail?>(null)
    val groupDetail: StateFlow<StudyGroupDetail?> = _groupDetail.asStateFlow()

    private val _interests = MutableStateFlow<List<Interest>>(emptyList())
    val interests: StateFlow<List<Interest>> = _interests.asStateFlow()

    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var requirements by mutableStateOf("")
    var selectedInterestName by mutableStateOf<String?>(null)
    var maxMembers by mutableStateOf("")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    // [수정] UI 이벤트를 한 번만 처리하기 위해 SharedFlow 사용
    private val _eventFlow = MutableSharedFlow<GroupEditEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    // [추가] 그룹 해산 확인 다이얼로그 상태
    private val _showDisbandDialog = MutableStateFlow(false)
    val showDisbandDialog: StateFlow<Boolean> = _showDisbandDialog.asStateFlow()

    init {
        if (groupId != -1L) {
            fetchGroupDetails()
            fetchAllInterests()
        } else {
            _errorMessage.value = "유효하지 않은 그룹 ID 입니다."
        }
    }

    private fun fetchAllInterests() {
        viewModelScope.launch {
            try {
                _interests.value = interestRepository.getAllInterests()
            } catch (e: Exception) {
                Log.e("GroupEditViewModel", "관심사 목록 로드 실패: ${e.message}", e)
            }
        }
    }

    private fun fetchGroupDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val detail = groupRepository.getGroupDetail(groupId)
                _groupDetail.value = detail
                title = detail.title
                description = detail.description
                requirements = detail.requirements
                selectedInterestName = detail.interestName
                maxMembers = detail.maxMembers.toString()
                Log.d("GroupEditViewModel", "그룹 상세 정보 로드 성공: $detail")
            } catch (e: Exception) {
                Log.e("GroupEditViewModel", "그룹 상세 정보 로드 실패: ${e.message}", e)
                _errorMessage.value = "그룹 정보를 불러오지 못했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateGroupInfo(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (groupId == -1L) {
            onError("잘못된 그룹 ID로 수정할 수 없습니다.")
            return
        }

        val currentMaxMembers = maxMembers.toIntOrNull()
        // 현재 멤버 수보다 작게 수정하는 것을 방지 (groupDetail이 null이 아닐 때만)
        val currentActualMembers = _groupDetail.value?.currentMembers ?: 1
        if (currentMaxMembers == null || currentMaxMembers < currentActualMembers || currentMaxMembers > 99) {
            onError("최대 인원 수는 현재 인원($currentActualMembers)보다 크거나 같고 99명 이하여야 합니다.")
            return
        }
        if (title.isBlank()) {
            onError("그룹명은 필수 항목입니다.")
            return
        }
        if (selectedInterestName.isNullOrBlank()) {
            onError("카테고리는 필수 항목입니다.")
            return
        }

        // locationName은 기존 값을 사용 (API 명세상 수정 요청 DTO에 포함되지만, UI에서 수정하지 않음)
        val currentLocationName = _groupDetail.value?.locationName
        if (currentLocationName == null) {
            onError("기존 그룹의 위치 정보를 찾을 수 없습니다.")
            return
        }

        val request = GroupCreateRequest(
            title = title,
            description = description,
            requirements = requirements,
            interest = selectedInterestName!!,
            maxMembers = currentMaxMembers,
            locationName = currentLocationName
        )

        viewModelScope.launch {
            _isUpdating.value = true
            try {
                val response = groupRepository.updateGroup(groupId, request)
                if (response.isSuccessful) {
                    _eventFlow.emit(GroupEditEvent.ShowToast("그룹 정보가 수정되었습니다."))
                    _eventFlow.emit(GroupEditEvent.NavigateBack)
                } else {
                    _eventFlow.emit(GroupEditEvent.ShowToast("수정 실패: ${response.code()}"))
                }
            } catch (e: Exception) {
                _eventFlow.emit(GroupEditEvent.ShowToast("오류 발생: ${e.message}"))
            } finally {
                _isUpdating.value = false
            }
        }
    }
    // ... (onTitleChange 등 UI 상태 업데이트 함수들은 동일)
    fun onTitleChange(newTitle: String) {
        title = newTitle
    }
    fun onDescriptionChange(newDescription: String) {
        description = newDescription
    }
    fun onRequirementsChange(newRequirements: String) {
        requirements = newRequirements
    }
    fun onInterestChange(newInterestName: String) {
        selectedInterestName = newInterestName
    }
    fun onMaxMembersChange(newMaxMembers: String) {
        maxMembers = newMaxMembers
    }

    fun onDisbandButtonClick() {
        _showDisbandDialog.value = true
    }

    // [추가] 다이얼로그 취소
    fun onDisbandDialogDismiss() {
        _showDisbandDialog.value = false
    }

    // [추가] 다이얼로그에서 '확인' 눌렀을 때 실제 그룹 해산 요청
    fun confirmDisbandGroup() {
        _showDisbandDialog.value = false
        viewModelScope.launch {
            try {
                val response = groupRepository.deactivateGroup(groupId)
                if (response.isSuccessful) {
                    _eventFlow.emit(GroupEditEvent.ShowToast("그룹이 해산되었습니다."))
                    _eventFlow.emit(GroupEditEvent.NavigateBack) // 성공 시 이전 화면으로 이동
                } else {
                    _eventFlow.emit(GroupEditEvent.ShowToast("그룹 해산에 실패했습니다: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e("GroupEditVM", "그룹 해산 실패", e)
                _eventFlow.emit(GroupEditEvent.ShowToast("오류가 발생했습니다."))
            }
        }
    }
}