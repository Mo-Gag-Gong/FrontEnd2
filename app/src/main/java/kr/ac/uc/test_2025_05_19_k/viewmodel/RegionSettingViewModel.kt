package kr.ac.uc.test_2025_05_19_k.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.data.local.UserPreference
import kr.ac.uc.test_2025_05_19_k.util.LocationUtils

class RegionSettingViewModel : ViewModel() {
    // 지역 설정 완료 여부 플래그
    private val _isRegionSet = MutableStateFlow(false)
    val isRegionSet: StateFlow<Boolean> get() = _isRegionSet

    // 지역 설정 완료 처리
    fun setRegionSet(value: Boolean) {
        _isRegionSet.value = value
    }

    // 네비게이션 이후 재진입 방지용 리셋
    fun resetRegionSet() {
        _isRegionSet.value = false
    }

    // 위치 요청 → 지역명 변환 → 캐시에 저장
    fun requestAndSaveLocation(context: Context) {
        viewModelScope.launch {
            val location = LocationUtils.getCurrentLocation(context)
            location?.let {
                val region = LocationUtils.getRegionFromLocation(context, it.latitude, it.longitude)
                region?.let {
                    UserPreference(context).saveLocation(it)
                    setRegionSet(true)
                }
            }
        }
    }


}
