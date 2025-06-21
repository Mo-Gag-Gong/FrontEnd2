// app/src/main/java/kr/ac/uc/test_2025_05_19_k/navigation/BottomNavigationBar.kt
package kr.ac.uc.test_2025_05_19_k.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday // 또는 DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color // Color 임포트 추가
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp // Elevation 등에 사용 가능
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", Icons.Filled.Home)
    object Schedule : BottomNavItem("schedule", Icons.Filled.CalendarToday) // 또는 DateRange
    object GroupManagement : BottomNavItem("group_management",  Icons.Filled.Groups)
    object MyProfile : BottomNavItem("my_profile",  Icons.Filled.Person)
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Schedule,
    BottomNavItem.GroupManagement,
    BottomNavItem.MyProfile
)

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface, // 하단 네비게이션 바 전체 배경색
        tonalElevation = 0.dp // 기본 그림자 효과 제거 또는 조정 (디자인에 따라)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        bottomNavItems.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            NavigationBarItem(
                icon = { Icon(screen.icon, null) },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary, // 선택된 아이콘 색상
                    selectedTextColor = MaterialTheme.colorScheme.primary, // 선택된 텍스트 색상
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant, // 선택되지 않은 아이콘 색상
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant, // 선택되지 않은 텍스트 색상
                    // PDF 디자인에 선택된 아이템 배경 강조가 뚜렷하지 않으므로, indicator를 투명하게 하거나 은은하게 변경
                    indicatorColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
                    // 또는 indicatorColor = Color.Transparent // 완전히 배경 강조를 없애려면
                ),
                alwaysShowLabel = false,
                label = null // 라벨을 명시적으로 null 처리
            )
        }
    }
}