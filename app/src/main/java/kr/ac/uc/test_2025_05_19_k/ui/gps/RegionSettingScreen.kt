package kr.ac.uc.test_2025_05_19_k.ui.gps

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import com.google.android.gms.location.LocationServices

import androidx.activity.result.contract.ActivityResultContracts
import kr.ac.uc.test_2025_05_19_k.viewmodel.RegionSettingViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.CoroutineScope
import kr.ac.uc.test_2025_05_19_k.data.local.UserPreference
import kr.ac.uc.test_2025_05_19_k.viewmodel.ProfileInputViewModel
import kr.ac.uc.test_2025_05_19_k.viewmodel.submitProfile

import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import android.content.IntentSender
import androidx.activity.result.IntentSenderRequest


// 현재 네비게이션 스택 기록용
@Composable
fun RememberedNavStack(navController: NavController): List<String> {
    val stack = remember { mutableStateListOf<String>() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        val route = navBackStackEntry?.destination?.route
        if (route != null) {
            if (stack.isEmpty() || stack.last() != route) {
                stack.add(route)
            }
        }
    }
    return stack
}

// SharedPreferences에 위치 저장 함수
fun saveLocationToPrefs(context: Context, cityName: String) {
    val prefs: SharedPreferences = context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
    prefs.edit().apply {
        putString("city_name", cityName)
        apply()
    }
}

// 현재 위치 받아오는 suspend 함수 (FusedLocationProviderClient)
suspend fun getCurrentLocation(context: Context): Location? = withContext(Dispatchers.IO) {
    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    if (!hasFine && !hasCoarse) return@withContext null

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    try {
        suspendCancellableCoroutine<Location?> { cont ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { loc -> cont.resume(loc, null) }
                .addOnFailureListener { cont.resume(null, null) }
        }
    } catch (e: Exception) {
        null
    }
}

// 좌표로부터 시/도명 반환
suspend fun getCityNameFromLocation(context: Context, latitude: Double, longitude: Double): String? =
    withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.KOREA)
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val rawName = addresses[0].adminArea ?: return@withContext null
                return@withContext simplifyRegionName(rawName)
            } else null
        } catch (e: Exception) {
            null
        }
    }

// 시/도명을 정제하여 '서울특별시' → '서울' 등으로 변환하는 함수
// 시/도명을 간단한 약칭으로 변환하는 함수
fun simplifyRegionName(raw: String): String {
    return when {
        raw.contains("서울") -> "서울"
        raw.contains("부산") -> "부산"
        raw.contains("대구") -> "대구"
        raw.contains("인천") -> "인천"
        raw.contains("광주") -> "광주"
        raw.contains("대전") -> "대전"
        raw.contains("울산") -> "울산"
        raw.contains("세종") -> "세종"
        raw.contains("경기도") -> "경기"
        raw.contains("강원도") -> "강원"
        raw.contains("충청북도") -> "충북"
        raw.contains("충청남도") -> "충남"
        raw.contains("전라북도") -> "전북"
        raw.contains("전라남도") -> "전남"
        raw.contains("경상북도") -> "경북"
        raw.contains("경상남도") -> "경남"
        raw.contains("제주") -> "제주"
        else -> raw.replace("특별시", "")
            .replace("광역시", "")
            .replace("도", "")
            .replace("특별자치도", "")
            .replace("특별자치시", "")
            .trim()
    }
}



// 위치 서비스(GPS) 활성화 여부 확인 함수
fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}
@Composable
fun RegionSettingScreen(
    navController: NavController,
    mode: String,
    interestIds: List<Long> = emptyList(),
    onBack: () -> Unit = {},
    onDone: (String) -> Unit = {},
    viewModel: RegionSettingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val profileViewModel: ProfileInputViewModel = hiltViewModel()

    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isLocationEnabledState by remember { mutableStateOf(isLocationEnabled(context)) }
    var regionName by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // 위치 권한 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        if (!isGranted) showPermissionDialog = true
    }

    // 시스템 GPS 다이얼로그 런처
    val gpsDialogLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            isLocationEnabledState = isLocationEnabled(context)

            if (isLocationEnabledState && permissionGranted && regionName == null) {
                coroutineScope.launch {
                    isLoading = true
                    val location = getCurrentLocation(context)
                    if (location != null) {
                        val city = getCityNameFromLocation(context, location.latitude, location.longitude)
                        if (!city.isNullOrBlank()) {
                            regionName = city

                            // ✅ 아래 부분이 추가된 핵심: mode에 따라 자동 저장 및 화면 이동
                            when (mode) {
                                "signup" -> {
                                    profileViewModel.updateLocation(city)
                                    profileViewModel.updateSelectedInterests(interestIds)
                                    profileViewModel.submitProfile(
                                        onSuccess = {
                                            navController.navigate("next_signup_step") {
                                                popUpTo("region_setting_signup") { inclusive = true }
                                            }
                                        },
                                        onError = {
                                            Toast.makeText(context, "프로필 저장 실패: $it", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                "cache" -> {
                                    UserPreference(context).saveLocation(city)
                                    viewModel.setRegionSet(true)
                                    navController.navigate("home") {
                                        popUpTo("region_setting_cache") { inclusive = true }
                                    }
                                }
                                "edit" -> {
                                    profileViewModel.updateLocation(city)
                                    profileViewModel.submitProfile(
                                        onSuccess = { navController.popBackStack() },
                                        onError = { msg ->
                                            Toast.makeText(context, "위치 수정 실패: $msg", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        } else {
                            errorMsg = "주소를 불러올 수 없습니다."
                        }
                    } else {
                        errorMsg = "위치 정보를 불러올 수 없습니다."
                    }
                    isLoading = false
                }
            }
        }
    }



    // 시스템 GPS 설정 다이얼로그 요청
    fun requestEnableGps() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 1000
        ).build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(e.resolution).build()
                    gpsDialogLauncher.launch(intentSenderRequest)
                } catch (ex: IntentSender.SendIntentException) {
                    Toast.makeText(context, "GPS 설정 요청 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 위치 자동 조회
    LaunchedEffect(permissionGranted, isLocationEnabledState, regionName) {
        isLocationEnabledState = isLocationEnabled(context)
        if (permissionGranted && isLocationEnabledState && regionName == null) {
            isLoading = true
            val location = getCurrentLocation(context)
            if (location != null) {
                val city = getCityNameFromLocation(context, location.latitude, location.longitude)
                if (!city.isNullOrBlank()) {
                    regionName = city
                } else {
                    errorMsg = "주소를 불러올 수 없습니다."
                }
            } else {
                errorMsg = "위치 정보를 불러올 수 없습니다."
            }
            isLoading = false
        }
    }

    // 화면 UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // 상단 바
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    regionName?.let { selectedRegion ->
                        when (mode) {
                            "signup" -> {
                                profileViewModel.updateLocation(selectedRegion)
                                profileViewModel.updateSelectedInterests(interestIds)
                                profileViewModel.submitProfile(
                                    onSuccess = {
                                        navController.navigate("next_signup_step") {
                                            popUpTo("region_setting_signup") { inclusive = true }
                                        }
                                    },
                                    onError = {
                                        Toast.makeText(context, "프로필 저장 실패: $it", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            "cache" -> {
                                UserPreference(context).saveLocation(selectedRegion)
                                viewModel.setRegionSet(true)
                                navController.navigate("home") {
                                    popUpTo("region_setting_cache") { inclusive = true }
                                }
                            }
                            "edit" -> {
                                profileViewModel.updateLocation(selectedRegion)
                                profileViewModel.submitProfile(
                                    onSuccess = { navController.popBackStack() },
                                    onError = { msg ->
                                        Toast.makeText(context, "위치 수정 실패: $msg", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14C7E5)),
                shape = RoundedCornerShape(16.dp),
                enabled = regionName != null && !isLoading
            ) {
                Text("완료!", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(40.dp))

        when {
            !permissionGranted -> {
                Text("위치 권한이 필요합니다.", color = Color.Red, fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
                    Text("권한 허용하기")
                }
            }

            !isLocationEnabledState -> {
                Text("위치 서비스(GPS)가 꺼져 있습니다.", color = Color.Red, fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                Button(onClick = { requestEnableGps() }) {
                    Text("위치 서비스 켜기")
                }
            }

            isLoading -> {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            regionName != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("내 위치: $regionName", fontWeight = FontWeight.Bold, fontSize = 28.sp, modifier = Modifier.padding(vertical = 20.dp))
                    Text("확인 후 '완료!'를 눌러주세요.", fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            regionName = null
                            val location = getCurrentLocation(context)
                            if (location != null) {
                                val city = getCityNameFromLocation(context, location.latitude, location.longitude)
                                regionName = city
                            }
                            isLoading = false
                        }
                    }) {
                        Text("다시 위치 조회")
                    }
                }
            }

            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    errorMsg?.let {
                        Text(it, color = Color.Red, fontSize = 18.sp, modifier = Modifier.padding(vertical = 12.dp))
                    }
                    Button(onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            val location = getCurrentLocation(context)
                            if (location != null) {
                                val city = getCityNameFromLocation(context, location.latitude, location.longitude)
                                regionName = city
                            }
                            isLoading = false
                        }
                    }) {
                        Text("내 위치 자동으로 찾기")
                    }
                }
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("권한 필요") },
            text = { Text("앱 사용을 위해 위치 권한을 허용해야 합니다.\\n\\n설정화면에서 권한을 허용해주세요.") },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.parse("package:" + context.packageName)
                    }
                    context.startActivity(intent)
                    showPermissionDialog = false
                }) {
                    Text("설정으로 이동")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}


// 위치를 가져와서 위도/경도/지역명 상태 업데이트
private fun CoroutineScope.fetchLocation(
    context: Context,
    onResult: (latitude: Double?, longitude: Double?, regionName: String?, error: String?) -> Unit
) {
    launch {
        var errorMsg: String? = null
        var regionName: String? = null
        var latitude: Double? = null
        var longitude: Double? = null

        // 위치 권한 확인
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            errorMsg = "위치 권한이 필요합니다."
            onResult(null, null, null, errorMsg)
            return@launch
        }

        // 위치 서비스 상태 확인
        if (!isLocationEnabled(context)) {
            errorMsg = "위치 서비스(GPS)가 꺼져 있습니다."
            onResult(null, null, null, errorMsg)
            return@launch
        }

        val location = getCurrentLocation(context)
        if (location != null) {
            latitude = location.latitude
            longitude = location.longitude
            val city = getCityNameFromLocation(context, latitude!!, longitude!!)
            if (city != null) {
                regionName = city
            } else {
                errorMsg = "주소를 불러올 수 없습니다."
            }
        } else {
            errorMsg = "위치 정보를 불러올 수 없습니다."
        }

        onResult(latitude, longitude, regionName, errorMsg)
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 387, heightDp = 812)
@Composable
fun PreviewRegionSettingScreen() {
    val navController = rememberNavController()
    RegionSettingScreen(
        navController = navController,
        mode = "cache", // 🔄 캐시 모드로 테스트
        interestIds = emptyList(),
        onBack = { /* 뒤로가기 프리뷰 무시 */ },
        onDone = { region ->
            println("✅ onDone 호출됨. 저장된 지역: $region")
            navController.navigateUp() // 캐시 모드일 경우 이전 화면으로 돌아가는 예시
        }
    )
}
