package kr.ac.uc.test_2025_05_19_k.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kr.ac.uc.test_2025_05_19_k.network.ApiService // ✅ 실제 ApiService 경로로 맞추기

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val api: ApiService // ApiService를 주입받아 사용
) : ViewModel() {

    // 온보딩 완료 여부 (null: 미확인, true/false: 확인됨)
    var onboardingCompleted by mutableStateOf<Boolean?>(null)
        private set

    /**
     * 서버에서 온보딩(프로필 입력) 완료 여부를 조회
     * @param onResult true: 온보딩 완료 → 홈으로, false: 미완료 → 로그인/온보딩
     */
    fun checkOnboardingStatus(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // 서버에 온보딩 상태 요청
                val res = api.getOnboardingStatus()
                onboardingCompleted = res.onboardingCompleted
                onResult(res.onboardingCompleted)
            } catch (e: Exception) {
                // 네트워크 오류 시 기본값 false (필요시 에러 안내 화면 추가)
                onResult(false)
            }
        }
    }





}
