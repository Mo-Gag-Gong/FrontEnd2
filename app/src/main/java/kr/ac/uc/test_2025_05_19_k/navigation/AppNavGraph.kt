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
import kr.ac.uc.test_2025_05_19_k.ui.group.NoticeCreateScreen
import java.net.URLDecoder // URL 디코딩을 위해 추가
import java.nio.charset.StandardCharsets
import kr.ac.uc.test_2025_05_19_k.ui.group.NoticeEditScreen

import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import kr.ac.uc.test_2025_05_19_k.ui.group.GroupGoalCreateEditScreen
import kr.ac.uc.test_2025_05_19_k.ui.group.GroupGoalDetailScreen
import kr.ac.uc.test_2025_05_19_k.ui.group.GroupGoalListScreen
import kr.ac.uc.test_2025_05_19_k.ui.group.GroupMemberDetailScreen
import kr.ac.uc.test_2025_05_19_k.ui.group.GroupMemberManageScreen
import kr.ac.uc.test_2025_05_19_k.ui.group.detail.JoinedGroupDetailScreen
import kr.ac.uc.test_2025_05_19_k.viewmodel.InterestSelectViewModel
import kr.ac.uc.test_2025_05_19_k.viewmodel.OnboardingViewModel
import kr.ac.uc.test_2025_05_19_k.viewmodel.ProfileInputViewModel
import kr.ac.uc.test_2025_05_19_k.ui.group.detail.GroupApplyScreen

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
            HomeScreen(
                navController = navController,
                // [수정] group 객체를 받아 isMember 값에 따라 분기 처리
                onGroupClick = { group ->
                    if (group.isMember) {
                        // 이미 가입한 그룹 -> 참여자용 상세 탭 화면으로 이동
                        navController.navigate("group_detail/${group.groupId}")
                    } else {
                        // 가입하지 않은 그룹 -> 가입 신청 화면으로 이동
                        navController.navigate("group_apply/${group.groupId}")
                    }
                },
                onCreateGroupClick = {
                    navController.navigate("group_create")
                },
                onNavigateToSearch = {
                    navController.navigate("search")
                }
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
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getLong("groupId") ?: -1L
            if (groupId != -1L) {
                NoticeCreateScreen(navController = navController, groupId = groupId)
            }
        }
        composable(
            route = "notice_edit/{groupId}/{noticeId}?title={title}&content={content}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.LongType },
                navArgument("noticeId") { type = NavType.LongType },
                navArgument("title") { type = NavType.StringType },
                navArgument("content") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getLong("groupId") ?: -1L
            val noticeId = backStackEntry.arguments?.getLong("noticeId") ?: -1L
            // URL로 전달된 문자열은 디코딩해야 할 수 있습니다.
            val title = backStackEntry.arguments?.getString("title")?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val content = backStackEntry.arguments?.getString("content")?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""

            if (groupId != -1L && noticeId != -1L) {
                NoticeEditScreen(
                    navController = navController,
                    groupId = groupId,
                    noticeId = noticeId,
                    initialTitle = title,
                    initialContent = content
                )
            }
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
        composable("search_result/{query}") { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: ""
            SearchResultScreen(
                navController = navController,
                searchQuery = query,
                onGroupClick = { groupId ->
                    navController.navigate("group_apply/$groupId")
                }
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

        // 그룹 목표 상세 화면
        composable(
            route = "group_goal_detail/{groupId}/{goalId}?isAdmin={isAdmin}",
            arguments = listOf(
                // [수정] NavType을 StringType으로 변경하여 ViewModel과 타입을 맞춥니다.
                navArgument("groupId") { type = NavType.StringType },
                navArgument("goalId") { type = NavType.StringType },
                navArgument("isAdmin") { type = NavType.BoolType; defaultValue = true }
            )
        ) { backStackEntry ->
            // [수정] 인자를 String으로 추출하여 GroupGoalDetailScreen에 전달합니다.
            val groupId = backStackEntry.arguments?.getString("groupId")
            val goalId = backStackEntry.arguments?.getString("goalId")

            if (groupId != null && goalId != null) {
                GroupGoalDetailScreen(
                    navController = navController,
                    groupId = groupId,
                    goalId = goalId
                )
            } else {
                Text("오류: 유효하지 않은 목표 정보입니다.")
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
