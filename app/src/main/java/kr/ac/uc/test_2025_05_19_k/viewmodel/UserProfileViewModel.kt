package kr.ac.uc.test_2025_05_19_k.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.model.UserProfileResponse
import kr.ac.uc.test_2025_05_19_k.model.UserProfileWithStatsDto
import kr.ac.uc.test_2025_05_19_k.repository.TokenManager
import kr.ac.uc.test_2025_05_19_k.repository.UserRepository
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val repository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    // 사용자 프로필 상태 (UI에서 관찰)
    private val _userProfile = mutableStateOf<UserProfileWithStatsDto?>(null)
    val userProfile: State<UserProfileWithStatsDto?> = _userProfile

    /**
     * SharedPreferences에서 userId 가져와 서버에서 프로필 요청
     */
    fun loadUserProfile() {
        viewModelScope.launch {
            val userId: Long = tokenManager.getUserId() ?: return@launch

            val result = repository.getUserProfile(userId)
            result.onSuccess {
                _userProfile.value = it
            }.onFailure {
                // 오류 처리
                println("프로필 가져오기 실패: ${it.message}")
            }

        }
    }
}
