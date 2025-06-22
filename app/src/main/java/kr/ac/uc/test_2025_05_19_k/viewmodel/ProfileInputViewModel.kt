package kr.ac.uc.test_2025_05_19_k.viewmodel

import android.app.Application
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.data.local.UserPreference
import kr.ac.uc.test_2025_05_19_k.model.*
import kr.ac.uc.test_2025_05_19_k.network.ApiService
import kr.ac.uc.test_2025_05_19_k.repository.*

@HiltViewModel
class ProfileInputViewModel @Inject constructor(
    val cacheManager: ProfileCacheManager,
    val api: ApiService,
    private val repository: ProfileRepository,
    private val tokenManager: TokenManager, application: Application
) : AndroidViewModel(application) {

    fun getCachedLocation(): String? {
        val context = getApplication<Application>().applicationContext
        return UserPreference(context).getLocation()
    }

    private val _userProfile = MutableStateFlow<UserProfileResponse?>(null)
    val userProfile: StateFlow<UserProfileResponse?> = _userProfile

    var interests by mutableStateOf<List<Interest>>(emptyList())
    var selectedInterestIds by mutableStateOf<List<Long>>(emptyList())
    var name by mutableStateOf("")
    var gender by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var birthYear by mutableStateOf("")
    var birthMonth by mutableStateOf("")
    var birthDay by mutableStateOf("")
    var locationName by mutableStateOf("")

    init {
        // 캐시에서 불러오기
        cacheManager.loadProfile()?.let {
            name = it.name
            gender = it.gender
            phoneNumber = it.phone
            locationName = it.location
            val parts = it.birth.split("-")
            if (parts.size == 3) {
                birthYear = parts[0]
                birthMonth = parts[1]
                birthDay = parts[2]
            }
        }
        selectedInterestIds = cacheManager.loadInterests()
    }

    fun loadUserProfile(userId: Long? = tokenManager.getUserId()) {
        if (userId == null) return

        viewModelScope.launch {
            try {
                val result = repository.getUserProfile(userId)
                result.onSuccess {
                    _userProfile.value = it

                    name = it.name
                    gender = it.gender.toString()
                    phoneNumber = it.phoneNumber ?: ""

                    // ✅ 위치는 서버에서 가져오지 않고 캐시에서만 가져옴
                    val cachedLocation = UserPreference(getApplication<Application>()).getLocation()
                    Log.d("SubmitDebug", "캐시에서 가져온 위치: $cachedLocation")
                    locationName = cachedLocation ?: ""

                    // 생년월일 파싱
                    it.birthYear?.let { b ->
                        val str = b.toString()
                        birthYear = str.take(4)
                        birthMonth = str.drop(4).take(2)
                        birthDay = str.drop(6).take(2)
                    }

                    selectedInterestIds = it.interests.map { i -> i.interestId }
                }
            } catch (e: Exception) {
                Log.e("loadUserProfile", e.message ?: "오류")
            }
        }
    }



    fun getBirthAsInt(): Int? {
        return try {
            val y = birthYear.padStart(4, '0')
            val m = birthMonth.padStart(2, '0')
            val d = birthDay.padStart(2, '0')
            (y + m + d).toInt()
        } catch (e: Exception) { null }
    }

    fun submitBasicProfileOnly(
        name: String,
        gender: String,
        phoneNumber: String,
        birthYear: Int,
        locationName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = userProfile.value
        val birth = getBirthAsInt()
        if (user == null || birth == null) {
            onError("기본 정보를 확인하세요.")
            return
        }

        val request = ProfileUpdateRequest(
            name = name.ifBlank { user.name },
            gender = gender.ifBlank { user.gender }.toString(),
            phoneNumber = phoneNumber.ifBlank { user.phoneNumber ?: "" },
            birthYear = birth,
            locationName = locationName,
            interestIds = user.interests.map { it.interestId }
        )

        Log.d("SubmitDebug", "기본정보 수정 요청: $request")

        viewModelScope.launch {
            try {
                // 🔴 기존 관심사 삭제
                user.interests.forEach {
                    runCatching {
                        api.deleteInterest(it.interestId)
                    }.onFailure { e ->
                        Log.w("SubmitDebug", "관심사 삭제 실패: ${e.message}")
                    }
                }

                // 🔵 프로필 업데이트
                val res = api.updateProfile(request)
                if (res.isSuccessful) {
                    cacheManager.saveProfile(
                        CachedProfile(
                            request.name,
                            request.gender,
                            request.phoneNumber,
                            birth.toString(),
                            request.locationName
                        )
                    )
                    onSuccess()
                } else {
                    onError("업데이트 실패: ${res.code()}")
                }
            } catch (e: Exception) {
                onError("오류 발생: ${e.message}")
            }
        }
    }



    fun submitInterests(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = userProfile.value
        if (user == null || selectedInterestIds.isEmpty()) {
            onError("관심사를 선택하세요.")
            return
        }

        val request = ProfileUpdateRequest(
            name = user.name,
            gender = user.gender.toString(),
            phoneNumber = user.phoneNumber ?: "",
            birthYear = user.birthYear ?: 0,
            locationName = user.locationName ?: "",
            interestIds = selectedInterestIds
        )

        Log.d("SubmitDebug", "관심사 수정 요청: $request")

        viewModelScope.launch {
            try {
                val res = api.updateProfile(request)
                if (res.isSuccessful) {
                    cacheManager.saveInterests(selectedInterestIds)
                    onSuccess()
                } else onError("업데이트 실패: ${res.code()}")
            } catch (e: Exception) {
                onError("예외: ${e.message}")
            }
        }
    }
    fun updateOnlyInterests(
        selectedInterestIds: List<Long>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = userProfile.value
        if (user == null) {
            onError("유저 정보를 불러오지 못했습니다.")
            return
        }

        viewModelScope.launch {
            try {
                // 1️⃣ 기존 관심사 모두 삭제
                user.interests.forEach {
                    runCatching {
                        api.deleteInterest(it.interestId)
                    }.onFailure { e ->
                        Log.w("InterestUpdate", "삭제 실패: ${e.message}")
                    }
                }

                // 2️⃣ 새로운 관심사 추가
                selectedInterestIds.forEach { id ->
                    runCatching {
                        api.addInterest(id)  // POST /api/users/interests/{interestId}
                    }.onFailure { e ->
                        Log.e("InterestUpdate", "추가 실패: ${e.message}")
                    }
                }

                // 성공 콜백
                onSuccess()
            } catch (e: Exception) {
                onError("관심사 수정 중 오류 발생: ${e.message}")
            }
        }
    }


    fun updateLocationOnly(newLocation: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = userProfile.value
        if (user == null) {
            onError("정보 없음")
            return
        }

        val request = ProfileUpdateRequest(
            name = user.name,
            gender = user.gender.toString(),
            phoneNumber = user.phoneNumber ?: "",
            birthYear = user.birthYear ?: 0,
            locationName = newLocation,
            interestIds = user.interests.map { it.interestId }
        )

        Log.d("SubmitDebug", "위치 수정 요청: $request")

        viewModelScope.launch {
            try {
                val res = api.updateProfile(request)
                if (res.isSuccessful) {
                    cacheManager.saveProfile(
                        CachedProfile(user.name,
                            user.gender.toString(), user.phoneNumber ?: "", user.birthYear?.toString() ?: "", newLocation)
                    )
                    onSuccess()
                } else onError("오류: ${res.code()}")
            } catch (e: Exception) {
                onError("예외 발생: ${e.message}")
            }
        }
    }

    // 현재 사용자 정보를 SharedPreferences 기반 캐시에 저장하는 함수
    private fun saveProfileToCache() {
        val birthFormatted = "${birthYear.padStart(4, '0')}-${birthMonth.padStart(2, '0')}-${birthDay.padStart(2, '0')}"
        cacheManager.saveProfile(
            CachedProfile(
                name = name,
                gender = gender,
                phone = phoneNumber,
                birth = birthFormatted,
                location = locationName
            )
        )
    }

    fun updateLocation(newLocation: String) {
        locationName = newLocation
        saveProfileToCache()
    }


    var interestLoading by mutableStateOf(false)
    var interestError by mutableStateOf<String?>(null)

    fun loadInterests() {
        viewModelScope.launch {
            try {
                interestLoading = true
                interestError = null
                interests = api.getAllInterests().map {
                    Interest(it.interestId, it.interestName)
                }
            } catch (e: Exception) {
                interestError = e.message
            } finally {
                interestLoading = false
            }
        }
    }

    fun toggleInterest(id: Long) {
        selectedInterestIds = if (selectedInterestIds.contains(id)) {
            selectedInterestIds - id
        } else {
            if (selectedInterestIds.size < 2) selectedInterestIds + id else selectedInterestIds
        }
        cacheManager.saveInterests(selectedInterestIds)
    }

    // 이름 변경
    fun updateName(newName: String) {
        name = newName
        saveProfileToCache() // 캐시에도 저장 (선택)
    }

    // 성별 변경
    fun updateGender(newGender: String) {
        gender = newGender
        saveProfileToCache()
    }

    // 전화번호 변경
    fun updatePhoneNumber(newPhoneNumber: String) {
        phoneNumber = newPhoneNumber
        saveProfileToCache()
    }

    // 생년월일(yyyy-MM-dd 형식의 전체 문자열)을 받아서 각각 분리 저장
    fun updateBirthYear(newBirthDate: String) {
        try {
            val parts = newBirthDate.split("-")
            if (parts.size == 3) {
                birthYear = parts[0]
                birthMonth = parts[1]
                birthDay = parts[2]
                saveProfileToCache()
            }
        } catch (e: Exception) {
            Log.e("updateBirthYear", "형식 오류: $newBirthDate", e)
        }
    }

    fun isLocationMissing(): Boolean {
        return locationName.isBlank()
    }

    fun loadCachedLocation() {
        val context = getApplication<Application>().applicationContext
        val cachedLocation = UserPreference(context).getLocation()
        if (!cachedLocation.isNullOrBlank()) {
            locationName = cachedLocation
        }
    }


    // ✅ 관심사 업데이트 함수 추가
    fun updateSelectedInterests(ids: List<Long>) {
        selectedInterestIds = ids
    }

    fun updateBirthYearPart(newYear: String) {
        birthYear = newYear
        saveProfileToCache()
    }

    fun updateBirthMonthPart(newMonth: String) {
        birthMonth = newMonth
        saveProfileToCache()
    }

    fun updateBirthDayPart(newDay: String) {
        birthDay = newDay
        saveProfileToCache()
    }







}

fun ProfileInputViewModel.submitProfile(
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val user = userProfile.value
    val birth = getBirthAsInt()

    if (user == null || birth == null) {
        onError("기본 정보를 확인하세요.")
        return
    }

    // 🔽 위치 정보가 비어 있으면 캐시에서 가져오기
    val resolvedLocationName = if (locationName.isNotBlank()) {
        locationName
    } else {
        // 캐시에서 불러오기
        val context = getApplication<Application>().applicationContext
        UserPreference(context).getLocation() ?: ""
    }

    val request = ProfileUpdateRequest(
        name = name.ifBlank { user.name },
        gender = gender.ifBlank { user.gender }.toString(),
        phoneNumber = phoneNumber.ifBlank { user.phoneNumber ?: "" },
        birthYear = birth,
        locationName = resolvedLocationName,
        interestIds = selectedInterestIds.ifEmpty { user.interests.map { it.interestId } }
    )

    Log.d("SubmitDebug", "최종 전송 request=$request")

    viewModelScope.launch {
        try {
            val res = api.updateProfile(request)
            if (res.isSuccessful) {
                // 캐시 저장 로직 생략
                onSuccess()
            } else {
                onError("업데이트 실패: ${res.code()}")
            }
        } catch (e: Exception) {
            onError("예외: ${e.message}")
        }
    }
}


fun ProfileInputViewModel.submitProfileDirect(
    name: String,
    gender: String,
    phoneNumber: String,
    birthYear: Int,
    locationName: String,
    interestIds: List<Long>,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val request = ProfileUpdateRequest(
        name = name,
        gender = gender,
        phoneNumber = phoneNumber,
        birthYear = birthYear,
        locationName = locationName,
        interestIds = interestIds
    )

    viewModelScope.launch {
        try {
            val res = api.updateProfile(request)
            if (res.isSuccessful) {
                cacheManager.saveProfile(
                    CachedProfile(name, gender, phoneNumber, birthYear.toString(), locationName)
                )
                cacheManager.saveInterests(interestIds)
                onSuccess()
            } else {
                onError("프로필 저장 실패: ${res.code()}")
            }
        } catch (e: Exception) {
            onError("예외 발생: ${e.message}")
        }
    }
}






