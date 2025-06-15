package kr.ac.uc.test_2025_05_19_k.ui.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kr.ac.uc.test_2025_05_19_k.model.GoalDetailDto
import kr.ac.uc.test_2025_05_19_k.viewmodel.GroupGoalDetailViewModel
import java.util.*
// ▼▼▼ [추가] 필요한 임포트 ▼▼▼
import kr.ac.uc.test_2025_05_19_k.util.toDate
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupGoalDetailScreen(
    navController: NavController,
    groupId: String,
    goalId: String,
    viewModel: GroupGoalDetailViewModel = hiltViewModel()
) {
    val goalDetail by viewModel.goalDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isCurrentUserAdmin by viewModel.isCurrentUserAdmin.collectAsState()

    val shouldRefreshState = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Boolean>("should_refresh_goals")
        ?.observeAsState()

    LaunchedEffect(shouldRefreshState?.value) {
        if (shouldRefreshState?.value == true) {
            viewModel.loadGoalDetails()
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.remove<Boolean>("should_refresh_goals")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(goalDetail?.title ?: "목표 상세") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    if (isCurrentUserAdmin) {
                        // ▼▼▼ [수정] java.time.LocalDate를 사용하여 목표 완료 여부 확인 ▼▼▼
                        val endDate = goalDetail?.endDate?.let { toDate(it) }
                        val isCompleted = endDate?.isBefore(LocalDate.now()) ?: false

                        TextButton(
                            onClick = {
                                navController.navigate("goal_edit/$groupId/$goalId")
                            },
                            enabled = !isCompleted // 목표가 이미 완료되었으면 비활성화
                        ) {
                            Text("수정")
                        }
                        TextButton(
                            onClick = {
                                viewModel.deleteGoal {
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("should_refresh_goals", true)
                                    navController.popBackStack()
                                }
                            },
                            enabled = !isCompleted // 목표가 이미 완료되었으면 비활성화
                        ) {
                            Text("삭제")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && goalDetail == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                goalDetail?.let { goal ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text("작성자: ${goal.creatorName}", style = MaterialTheme.typography.bodyMedium)
                            Text("기간: ${goal.startDate} ~ ${goal.endDate}", style = MaterialTheme.typography.bodyMedium)
                            Divider(modifier = Modifier.padding(vertical = 16.dp))
                        }
                        items(items = goal.details, key = { it.detailId ?: UUID.randomUUID().toString() }) { detail ->
                            GoalDetailItem(
                                detail = detail,
                                isEnabled = isCurrentUserAdmin,
                                onCheckChange = { viewModel.toggleDetailCompletion(detail.detailId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoalDetailItem(
    detail: GoalDetailDto,
    isEnabled: Boolean,
    onCheckChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = detail.isCompleted,
            onCheckedChange = onCheckChange,
            enabled = isEnabled
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = detail.description ?: "", style = MaterialTheme.typography.bodyLarge)
    }
}