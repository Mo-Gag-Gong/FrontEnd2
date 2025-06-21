package kr.ac.uc.test_2025_05_19_k.ui.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.ui.group.GroupGoalCard
import kr.ac.uc.test_2025_05_19_k.viewmodel.GroupAdminDetailViewModel
import androidx.compose.runtime.livedata.observeAsState

@OptIn(ExperimentalMaterial3Api::class)
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


    // ▼▼▼ [추가] BottomSheet 상태 관리 ▼▼▼
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedGoal by viewModel.selectedGoalDetail.collectAsState()
    val scope = rememberCoroutineScope()


    LaunchedEffect(shouldRefreshState?.value) {
        if (shouldRefreshState?.value == true) {
            // 1. 신호가 true이면, 강제로 목록을 새로고침
            viewModel.fetchGroupGoals(forceRefresh = true)
            // 2. 신호를 사용했으므로 다시 false로 만들어 중복 새로고침 방지
            navController.currentBackStackEntry?.savedStateHandle?.set("should_refresh_goals", false)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchGroupGoals()
    }

    // ▼▼▼ [추가] 선택된 목표가 있을 때만 BottomSheet를 띄움 ▼▼▼
    if (selectedGoal != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelectedGoal() },
            sheetState = sheetState
        ) {
            GoalDetailSheetContent(
                goal = selectedGoal!!,
                onEditClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        viewModel.clearSelectedGoal()
                        navController.navigate("goal_edit/$groupId/${selectedGoal!!.goalId}")
                    }
                },
                onDeleteClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        viewModel.deleteSelectedGoal {
                            viewModel.clearSelectedGoal()
                        }
                    }
                },
                onToggleDetail = { detailId ->
                    viewModel.toggleGoalDetailCompletion(detailId)
                }
            )
        }
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
                    GroupGoalCard(goal = goal) {
                        viewModel.onGoalSelected(goal.goalId)
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { navController.navigate("goal_create/$groupId") },
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