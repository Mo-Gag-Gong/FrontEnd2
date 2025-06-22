package kr.ac.uc.test_2025_05_19_k.ui.gps

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavBackStackEntry
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SignInGPSSettingScreen(
    backStackEntry: NavBackStackEntry,
    onBack: () -> Unit = {},
    onLocationGranted: (List<Long>) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    // 관심사 ID 추출
    val interestIdsParam = backStackEntry.arguments?.getString("interestIds") ?: ""
    val interestIds: List<Long> = interestIdsParam
        .split(",")
        .mapNotNull { it.toLongOrNull() }
        .filter { it > 0 }

    // 권한 상태 감지
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var gpsEnabled by remember { mutableStateOf(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) }

    // ✅ 앱이 다시 Resume 되었을 때 GPS 켜졌는지 확인
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                if (gpsEnabled && locationPermissionState.status.isGranted) {
                    onLocationGranted(interestIds) // ✅ 자동 화면 전환
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // 뒤로가기 버튼
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        }

        // 중앙 안내 및 버튼
        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("이제 위치 정보를 확인할게요!", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("위치 정보 확인 동의를 해주세요!", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(32.dp))

            // 🔵 위치 권한 요청 버튼
            Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                Text("위치 권한 동의")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🔵 GPS 설정 이동 버튼
            Button(onClick = {
                val intent = android.content.Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
            }) {
                Text("위치 서비스 설정으로 이동")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 상태 출력
            when {
                locationPermissionState.status.isGranted && gpsEnabled -> {
                    Text("위치 권한 + GPS 설정이 완료되었습니다.", color = Color.Green)
                }
                locationPermissionState.status.isGranted && !gpsEnabled -> {
                    Text("위치 권한은 허용되었지만 GPS가 꺼져있습니다.", color = Color.Red)
                }
                locationPermissionState.status.shouldShowRationale -> {
                    Text("위치 권한이 필요합니다.", color = Color.Red)
                }
                else -> {
                    Text("위치 권한이 아직 허용되지 않았습니다.", color = Color.Gray)
                }
            }
        }
    }
}



@Preview(showBackground = true, widthDp = 375, heightDp = 812)
@Composable
fun PreviewSignInGPSSettingScreen() {
    // 프리뷰에서는 backStackEntry 없이 간단한 화면 확인만 지원
    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text("GPS 권한 설정 프리뷰 화면")
    }
}

