// 파일: kr/ac/uc/test_2025_05_19_k/util/LocationUtils.kt

package kr.ac.uc.test_2025_05_19_k.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale

// 관심사 ID 리스트를 SharedPreferences에 저장
fun saveInterestIds(context: Context, ids: List<Long>) {
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    prefs.edit().putStringSet("interest_ids", ids.map { it.toString() }.toSet()).apply()
}

// SharedPreferences에서 관심사 ID 리스트 조회
fun getInterestIds(context: Context): List<Long> {
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    return prefs.getStringSet("interest_ids", emptySet())
        ?.mapNotNull { it.toLongOrNull() }
        ?: emptyList()
}




// 유저 정보 임시 저장
fun saveUserInputInfo(
    context: Context,
    name: String,
    gender: String,
    phone: String,
    birth: String
) {
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    prefs.edit()
        .putString("user_name", name)
        .putString("user_gender", gender)
        .putString("user_phone", phone)
        .putString("user_birth", birth)
        .apply()
}

// 유저 정보 임시 조회
fun getUserInputInfo(context: Context): UserInputInfo {
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    return UserInputInfo(
        name = prefs.getString("user_name", "") ?: "",
        gender = prefs.getString("user_gender", "") ?: "",
        phone = prefs.getString("user_phone", "") ?: "",
        birth = prefs.getString("user_birth", "") ?: ""
    )
}

// 데이터 클래스 (입력값 구조)
data class UserInputInfo(
    val name: String,
    val gender: String,
    val phone: String,
    val birth: String
)

suspend fun getCurrentLocation(context: Context): Location? = withContext(Dispatchers.IO) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // 권한 체크는 필수입니다.
    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasPermission) {
        Log.w("LocationUtils", "Location permission not granted.")
        return@withContext null
    }

    // suspendCancellableCoroutine을 사용하여 콜백 기반의 API를 코루틴에 맞게 변환합니다.
    suspendCancellableCoroutine { continuation ->
        // 1. 위치 요청 설정 (정확도, 갱신 주기 등)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(5000L)
            .setMaxUpdates(1) // 한 번만 위치를 받고 싶을 때 사용
            .build()

        // 2. 위치 업데이트를 수신할 콜백 정의
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // 4. 위치 정보를 성공적으로 받으면, 콜백을 제거하고 코루틴을 재개합니다.
                locationResult.lastLocation?.let { location ->
                    if (continuation.isActive) {
                        continuation.resume(location, null)
                    }
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        // 3. 위치 업데이트 요청
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        // 코루틴이 취소될 경우, 위치 업데이트 콜백도 함께 제거합니다.
        continuation.invokeOnCancellation {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}

suspend fun getCityNameFromLocation(context: Context, latitude: Double, longitude: Double): String? =
    withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.KOREA)
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (addresses?.isNotEmpty() == true) {
                // 1. Geocoder로부터 전체 행정구역명(adminArea)을 가져옵니다. (예: "울산광역시")
                val fullAdminArea = addresses[0].adminArea

                // ▼▼▼ [수정/추가] 전체 이름에서 접미사 제거 로직 ▼▼▼
                fullAdminArea?.let {
                    it.removeSuffix("광역시")
                        .removeSuffix("특별시")
                        .removeSuffix("특별자치도") // '제주특별자치도'와 같은 경우를 위해 '도'보다 먼저 처리
                        .removeSuffix("도")
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("LocationUtils", "Failed to convert location to city name.", e)
            null
        }
    }
