package kr.ac.uc.test_2025_05_19_k.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.repository.ProfileRepository
import javax.inject.Inject
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kr.ac.uc.test_2025_05_19_k.model.ProfileUpdateRequest
import kr.ac.uc.test_2025_05_19_k.model.UserProfileResponse

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,

) : ViewModel() {

    // ✅ 사용자 프로필 상태 관리용
    private val _userProfile = MutableStateFlow<UserProfileResponse?>(null)
    val userProfile: StateFlow<UserProfileResponse?> = _userProfile

    // 상태 변수들 (UI 바인딩용)
    var name by mutableStateOf("")
    var gender by mutableStateOf<String?>(null)
    var phoneNumber by mutableStateOf("")
    var birth by mutableStateOf("")
    var locationName by mutableStateOf("")
    var selectedInterestIds by mutableStateOf<List<Long>>(emptyList())

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)



    // 상태 업데이트 함수
    fun updateName(newName: String) { name = newName }
    fun updateGender(newGender: String?) { gender = newGender }
    fun updatePhoneNumber(newNumber: String) { phoneNumber = newNumber }
    fun updateBirthYear(newBirth: String) { birth = newBirth }
    fun updateLocationName(newLocation: String) { locationName = newLocation }
    fun updateSelectedInterests(newIds: List<Long>) { selectedInterestIds = newIds }

    // 프로필 저장 함수 (파라미터 없이 상태 기반)
    fun submitProfile(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Log.d("submitProfile", "입력값 확인 → name=$name, gender=$gender, phone=$phoneNumber, birth=$birth, location=$locationName, interests=$selectedInterestIds")

        Log.d("RegionSettingScreen", "submitProfile start, repository=$repository")
        isLoading = true
        errorMessage = null

        // 유효성 체크 (필수)
        val birth = birth.toIntOrNull()
        if (birth == null) {
            onError("생년월일(출생연도)을 올바르게 입력하세요.")
            isLoading = false
            return
        }
        if (name.isBlank()) {
            onError("이름을 입력하세요.")
            isLoading = false
            return
        }
        if (gender.isNullOrBlank()) {
            onError("성별을 선택하세요.")
            isLoading = false
            return
        }
        if (phoneNumber.isBlank()) {
            onError("전화번호를 입력하세요.")
            isLoading = false
            return
        }
        if (locationName.isBlank()) {
            onError("위치를 선택하세요.")
            isLoading = false
            return
        }
        if (selectedInterestIds.isEmpty()) {
            onError("관심사를 1개 이상 선택하세요.")
            isLoading = false
            return
        }

        // DTO로 변환
        val profileRequest = ProfileUpdateRequest(
            name = name,
            gender = gender!!,
            phoneNumber = phoneNumber,
            birthYear = birth,
            locationName = locationName,
            interestIds = selectedInterestIds
        )
        Log.d("ProfileRequest", profileRequest.toString())

        Log.d("RegionSettingScreen", "submitProfile 진입!!") // 여기도 추가

        viewModelScope.launch {
            Log.d("RegionSettingScreen", "코루틴 진입!!")
            try {
                Log.d("RegionSettingScreen", "서버 호출 직전: $profileRequest")
                val result = repository.updateProfile(profileRequest)
                Log.d("RegionSettingScreen", "서버 호출 완료: $result")
                if (result.isSuccessful) {
                    Log.d("RegionSettingScreen", "프로필 업데이트 성공!")
                    onSuccess()
                } else {
                    Log.e("RegionSettingScreen", "서버 오류: ${result.code()} / ${result.message()}")
                    onError("서버 오류: ${result.code()} / ${result.message()}")
                }
            } catch (e: Exception) {
                Log.e("RegionSettingScreen", "네트워크 예외 발생: ${e.localizedMessage}")
                onError("네트워크 오류: ${e.localizedMessage ?: "알 수 없는 오류"}")
            } finally {
                isLoading = false
                Log.d("RegionSettingScreen", "submitProfile finally!")
            }
        }
    }







}
