// app/src/main/java/kr/ac/uc/test_2025_05_19_k/navigation/AppNavGraph.kt
package kr.ac.uc.test_2025_05_19_k.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.*
import kotlinx.coroutines.delay
import kr.ac.uc.test_2025_05_19_k.ui.*
import kr.ac.uc.test_2025_05_19_k.ui.gps.RegionSettingScreen
import kr.ac.uc.test_2025_05_19_k.ui.group.create.GroupCreateScreen
import kr.ac.uc.test_2025_05_19_k.ui.home.HomeScreen
import kr.ac.uc.test_2025_05_19_k.ui.profile.SignInProfileSettingScreen
import kr.ac.uc.test_2025_05_19_k.ui.gps.SignInGPSSettingScreen
import kr.ac.uc.test_2025_05_19_k.ui.profile.InterestSelectScreenHost
import kr.ac.uc.test_2025_05_19_k.ui.profile.SignInScreen
import kr.ac.uc.test_2025_05_19_k.ui.schedule.ScheduleScreen
import kr.ac.uc.test_2025_05_19_k.ui.group.GroupManagementScreen
import kr.ac.uc.test_2025_05_19_k.ui.profile.MyProfileScreen
import kr.ac.uc.test_2025_05_19_k.ui.search.SearchScreen
import kr.ac.uc.test_2025_05_19_k.ui.search.SearchResultScreen
import kr.ac.uc.test_2025_05_19_k.ui.group.GroupAdminDetailScreen
import kr.ac.uc.test_2025_05_19_k.ui.group.GroupEditScreen

import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import kr.ac.uc.test_2025_05_19_k.ui.group.GroupGoalCreateEditScreen
import kr.ac.uc.test_2025_05_19_k.ui.group.GroupGoalDetailScreen
import kr.ac.uc.test_2025_05_19_k.ui.group.GroupGoalListScreen
import kr.ac.uc.test_2025_05_19_k.ui.group.GroupMemberDetailScreen
import kr.ac.uc.test_2025_05_19_k.ui.group.GroupMemberManageScreen
import kr.ac.uc.test_2025_05_19_k.ui.group.NoticeFormScreen
import kr.ac.uc.test_2025_05_19_k.ui.group.detail.JoinedGroupDetailScreen
import kr.ac.uc.test_2025_05_19_k.viewmodel.InterestSelectViewModel
import kr.ac.uc.test_2025_05_19_k.viewmodel.OnboardingViewModel
import kr.ac.uc.test_2025_05_19_k.viewmodel.ProfileInputViewModel
import kr.ac.uc.test_2025_05_19_k.ui.group.detail.GroupApplyScreen
import kr.ac.uc.test_2025_05_19_k.viewmodel.HomeViewModel

@Composable
fun LogCurrentScreen(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        val dest = navBackStackEntry?.destination
        val args = navBackStackEntry?.arguments
        val currentRoute = dest?.route
        val params = args?.keySet()?.joinToString { key -> "$key=${args.get(key)}" }
        while (true) {
            Log.d(
                "CurrentScreenLogger",
                "현재 화면(route): $currentRoute, params: $params"
            )
            delay(1000L)
        }
    }
}

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String
) {
    LogCurrentScreen(navController)

    NavHost(
        navController = navController,
        startDestination = "entry",
        modifier = modifier
    ) {
        // 🚩 엔트리 분기: 첫 진입시 홈/로그인 자동 분기
        composable("entry") {
            EntryScreen(navController)
        }

        // 1. 로그인 화면
        composable("login") {
            SignInScreen(
                onNavigateNext = {
                    navController.navigate("profile_input") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // 2. 프로필 입력
        composable("profile_input") {
            SignInProfileSettingScreen(
                navController = navController,
                onNext = { name, gender, phone, birth ->
                    if (name.isNotBlank() && gender.isNotBlank() && phone.isNotBlank() && birth.isNotBlank()) {
                        navController.navigate("interest_select/$name/$gender/$phone/$birth")
                    } else {
                        Log.w("NAV", "onNext 파라미터 비어있음: $name, $gender, $phone, $birth")
                    }
                }
            )
        }

        // 3. 관심사 선택
        composable(
            "interest_select/{name}/{gender}/{phone}/{birth}",
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("gender") { type = NavType.StringType },
                navArgument("phone") { type = NavType.StringType },
                navArgument("birth") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val gender = backStackEntry.arguments?.getString("gender") ?: ""
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            val birth = backStackEntry.arguments?.getString("birth") ?: ""

            val viewModel: InterestSelectViewModel = hiltViewModel()
            LaunchedEffect(Unit) {
                if (viewModel.userName.isBlank()) {
                    viewModel.setUserInfo(name, gender, phone, birth)
                }
            }

            InterestSelectScreenHost(
                navController = navController
            )
        }

        // 4. 위치 권한 요청
        composable(
            route = "gps_setting?interestIds={interestIds}",
            arguments = listOf(
                navArgument("interestIds") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            SignInGPSSettingScreen(
                backStackEntry = backStackEntry,
                onBack = { navController.popBackStack() },
                onLocationGranted = { interestIds ->
                    navController.navigate("region_setting?interestIds=${interestIds.joinToString(",")}") {
                        popUpTo("gps_setting") { inclusive = true }
                    }
                }
            )
        }

        // 5. 지역 선택/확인
        composable("region_setting") { backStackEntry ->
            val context = LocalContext.current
            val viewModel: ProfileInputViewModel = hiltViewModel()

            RegionSettingScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
                onDone = { selectedRegion: String ->
                    Log.d("RegionSettingScreen", "onDone 호출: $selectedRegion")
                    viewModel.updateLocation(selectedRegion)
                    Log.d(
                        "RegionSettingScreen",
                        "submitProfile 직전 값: name=${viewModel.name}, gender=${viewModel.gender}, phone=${viewModel.phoneNumber}, birth=${viewModel.birthYear}, interestIds=${viewModel.selectedInterestIds}, locationName=${viewModel.locationName}"
                    )
                    viewModel.submitProfile(
                        onSuccess = {
                            Log.d("RegionSettingScreen", "submitProfile 성공!")
                            navController.navigate(BottomNavItem.Home.route) {
                                popUpTo("region_setting") { inclusive = true }
                            }
                        },
                        onError = { msg: String ->
                            Log.e("RegionSettingScreen", "submitProfile 실패: $msg")
                            Toast.makeText(context, "프로필 저장 실패: $msg", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }

        // --- 하단 네비게이션 바가 있는 주요 화면 ---
        composable(BottomNavItem.Home.route) {
            // ▼▼▼ [수정] HomeScreen이 HomeViewModel을 직접 사용하도록 변경 ▼▼▼
            val homeViewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                navController = navController,
                viewModel = homeViewModel
            )
        }
        composable(BottomNavItem.Schedule.route) { ScheduleScreen(navController = navController) }
        composable(BottomNavItem.GroupManagement.route) { GroupManagementScreen(navController = navController) }
        composable(BottomNavItem.MyProfile.route) { MyProfileScreen(navController = navController) }


        // --- 그룹 상세/생성/관리 등 추가 화면 ---
        composable(
            route = "group_apply/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getLong("groupId")
            if (groupId != null) {
                // 이전에 GroupDetailScreen 이었던 가입 신청용 화면을 호출합니다.
                // 파일명을 GroupApplyScreen으로 바꾸시는 것을 권장합니다.
                GroupApplyScreen(
                    navController = navController,
                    groupId = groupId
                )
            } else {
                Text("오류: 유효하지 않은 그룹 ID입니다.")
            }
        }
        composable(
            route = "group_detail/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
        ) {
            JoinedGroupDetailScreen(navController = navController)
        }
        composable("group_create") {
            GroupCreateScreen(navController = navController)
        }
        composable(
            route = "group_edit/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getLong("groupId") ?: -1L
            if (groupId != -1L) {
                GroupEditScreen(navController = navController, groupId = groupId)
            } else {
                Text("오류: 유효하지 않은 그룹 ID 입니다.")
            }
        }
        composable(
            route = "group_admin_detail/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getLong("groupId") ?: -1L
            if (groupId != -1L) {
                GroupAdminDetailScreen(navController = navController, groupId = groupId)
            } else {
                Text("오류: 유효하지 않은 그룹 ID 입니다. (관리자 상세)")
            }
        }
        composable(
            route = "notice_create/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
        ) {
            NoticeFormScreen(navController = navController)
        }
        composable(
            route = "notice_edit/{groupId}/{noticeId}?title={title}&content={content}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.LongType },
                navArgument("noticeId") { type = NavType.LongType },
                // ViewModel의 SavedStateHandle에서 직접 값을 꺼내 쓰므로 타입만 명시
                navArgument("title") { type = NavType.StringType; nullable = true },
                navArgument("content") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            // URL에서 decode할 필요 없이 ViewModel에서 직접 처리하므로 UI 로직은 제거
            NoticeFormScreen(navController = navController)
        }



        // --- 검색 화면 ---
        composable("search") {
            SearchScreen(
                navController = navController,
                onSearch = { query ->
                    navController.navigate("search_result/$query")
                }
            )
        }
        composable(
            route = "search_result/{query}",
            arguments = listOf(navArgument("query") { type = NavType.StringType })
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: ""
            // ▼▼▼ [수정] onGroupClick 파라미터 제거 ▼▼▼
            SearchResultScreen(
                navController = navController,
                searchQuery = query
            )
        }

        composable(
            "group_member_manage/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
        ) {
            GroupMemberManageScreen(navController = navController)
        }
        composable(
            "group_member_detail/{groupId}/{userId}/{status}", // status 파라미터가 포함된 경로
            arguments = listOf(
                navArgument("groupId") { type = NavType.LongType },
                navArgument("userId") { type = NavType.LongType },
                navArgument("status") { type = NavType.StringType } // status의 타입을 String으로 정의
            )
        ) {
            // GroupMemberDetailScreen을 호출하는 부분은 그대로 둡니다.
            GroupMemberDetailScreen(navController = navController)
        }

        composable(
            route = "group_goal_list/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            if (groupId != null) {
                GroupGoalListScreen(navController = navController, groupId = groupId)
            }
        }


        // 그룹 목표 생성 화면
        composable(
            route = "goal_create/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            // goalId는 선택적이므로, 이 라우트에서는 ViewModel이 null로 받게 됩니다.
            GroupGoalCreateEditScreen(navController = navController)
        }

        // 그룹 목표 수정 화면
        composable(
            route = "goal_edit/{groupId}/{goalId}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("goalId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // goalId가 존재하므로, ViewModel은 자동으로 수정 모드로 동작합니다.
            GroupGoalCreateEditScreen(navController = navController)
        }
    }

}

// Splash-like 자동 분기용 EntryScreen
@Composable
fun EntryScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val checked = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkOnboardingStatus { completed ->
            if (completed) {
                navController.navigate(BottomNavItem.Home.route) {
                    popUpTo("entry") { inclusive = true }
                }
            } else {
                navController.navigate("login") {
                    popUpTo("entry") { inclusive = true }
                }
            }
            checked.value = true
        }
    }
    if (!checked.value) {
        CircularProgressIndicator()
    }
}
