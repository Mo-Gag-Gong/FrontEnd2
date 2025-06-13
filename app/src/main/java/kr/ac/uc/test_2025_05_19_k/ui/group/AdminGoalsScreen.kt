package kr.ac.uc.test_2025_05_19_k.ui.group

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kr.ac.uc.test_2025_05_19_k.model.GroupGoalDto
import kr.ac.uc.test_2025_05_19_k.viewmodel.GroupAdminDetailViewModel
import kr.ac.uc.test_2025_05_19_k.ui.group.common.GoalItem
import androidx.compose.runtime.livedata.observeAsState

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

    // 이 화면이 보일 때만 데이터를 불러오도록 LaunchedEffect 사용
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = goals, key = { it.goalId }) { goal ->
                    GoalItem(goal = goal) {
                        navController.navigate("group_goal_detail/$groupId/${goal.goalId}")
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                navController.navigate("goal_create/$groupId")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "그룹 목표 추가")
        }
    }
}