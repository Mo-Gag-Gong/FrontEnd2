package kr.ac.uc.test_2025_05_19_k.ui.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kr.ac.uc.test_2025_05_19_k.ui.group.GroupGoalCard
import kr.ac.uc.test_2025_05_19_k.viewmodel.GroupAdminDetailViewModel

@Composable
fun AdminGoalsScreen(
    navController: NavController,
    groupId: Long,
    viewModel: GroupAdminDetailViewModel
) {
    val goals by viewModel.goals.collectAsState()
    val isLoading by viewModel.isLoadingGoals.collectAsState()
    val shouldRefreshState = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Boolean>("should_refresh_goals")
        ?.observeAsState()

    LaunchedEffect(shouldRefreshState?.value) {
        if (shouldRefreshState?.value == true) {
            viewModel.fetchGroupGoals(forceRefresh = true)
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.remove<Boolean>("should_refresh_goals")
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.fetchGroupGoals()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (goals.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("등록된 그룹 목표가 없습니다.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            // ▼▼▼ [수정] LazyColumn에서 GroupGoalCard 사용 ▼▼▼
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = goals, key = { it.goalId }) { goal ->
                    GroupGoalCard(goal = goal) {
                        navController.navigate("group_goal_detail/$groupId/${goal.goalId}")
                    }
                }
            }
        }

        // ▼▼▼ [수정] FAB를 ExtendedFloatingActionButton으로 변경 ▼▼▼
        ExtendedFloatingActionButton(
            onClick = {
                navController.navigate("goal_create/$groupId")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            text = { Text("목표 추가") },
            icon = { Icon(Icons.Default.Add, contentDescription = "그룹 목표 추가") },
            containerColor = Color(0xFF00B2FF),
            contentColor = Color.White
        )
    }
}