// MainActivity.kt
package kr.ac.uc.test_2025_05_19_k

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.data.local.UserPreference
import kr.ac.uc.test_2025_05_19_k.navigation.AppNavGraph
import kr.ac.uc.test_2025_05_19_k.navigation.BottomNavItem
import kr.ac.uc.test_2025_05_19_k.navigation.BottomNavigationBar
import kr.ac.uc.test_2025_05_19_k.navigation.bottomNavItems
import kr.ac.uc.test_2025_05_19_k.ui.theme.MogackoTheme
import kr.ac.uc.test_2025_05_19_k.repository.TokenManager
import kr.ac.uc.test_2025_05_19_k.util.AppEvents
import kr.ac.uc.test_2025_05_19_k.util.LocationUtils.getCityNameFromLocation
import kr.ac.uc.test_2025_05_19_k.util.LocationUtils.getCurrentLocation
import kr.ac.uc.test_2025_05_19_k.util.RefreshEvent
import javax.inject.Inject
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Divider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import android.view.Window

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreference: UserPreference
    @Inject
    lateinit var tokenManager: TokenManager
    @Inject
    lateinit var appEvents: AppEvents
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("MainActivity", "위치 권한 승인됨. 위치 조회 재시도.")
                lifecycleScope.launch { fetchAndSaveUserLocation() }
            } else {
                Log.d("MainActivity", "위치 권한 거부됨.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContent {
            MogackoTheme { // 앱 전체 테마 적용
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // NavController 생성 (NavHost 관리)
                    val navController = rememberNavController()

                    // 토큰과 온보딩 완료 여부 체크
                    val isLoggedIn = tokenManager.hasValidToken()
                    val isOnboardingComplete = userPreference.isOnboardingCompleted()

                    // 시작 화면 라우트 결정
                    val startDestination = when {
                        isLoggedIn && isOnboardingComplete -> BottomNavItem.Home.route
                        isLoggedIn && !isOnboardingComplete -> "profile_input"
                        else -> "login"
                    }

                    // 메인 화면 호출 (하단바, 네비게이션 등 관리)
                    MainScreen(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }

        }
        checkLoginAndFetchLocation()
    }
    private fun checkLoginAndFetchLocation() {
        lifecycleScope.launch {
            if (!tokenManager.getAccessToken().isNullOrBlank()) {
                when {
                    ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        fetchAndSaveUserLocation()
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            }
        }
    }

    private suspend fun fetchAndSaveUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val location = getCurrentLocation(this@MainActivity) //
            if (location != null) {
                val cityName = getCityNameFromLocation(this@MainActivity, location.latitude, location.longitude) //
                if (!cityName.isNullOrBlank()) {
                    userPreference.saveRegion(cityName) //
                    Log.i("MainActivity", "현재 위치를 성공적으로 저장했습니다: $cityName")

                    // ▼▼▼ [추가] 위치 저장 후 갱신 이벤트 발생시키기 ▼▼▼
                    appEvents.emitEvent(RefreshEvent.LocationUpdated)
                } else {
                    Log.w("MainActivity", "좌표를 지역명으로 변환하는 데 실패했습니다.")
                }
            } else {
                Log.w("MainActivity", "현재 위치 정보를 가져올 수 없습니다.")
            }
        }
    }
}

@Composable
fun MainScreen(navController: androidx.navigation.NavHostController, startDestination: String) {
    // 현재 라우트 확인 (하단 네비게이션바 노출 여부 결정)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val shouldShowBottomBar = bottomNavItems.any { it.route == currentRoute }

    androidx.compose.material3.Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                Column {
                    Divider(thickness = 1.dp, color = Color.Black.copy(alpha = 0.5f))
                    BottomNavigationBar(navController = navController)
                }
            }
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding) // NavHost에 패딩 적용
        )
    }
}
