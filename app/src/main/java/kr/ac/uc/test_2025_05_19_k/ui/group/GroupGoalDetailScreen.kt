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
import kr.ac.uc.test_2025_05_19_k.util.toDate
import kr.ac.uc.test_2025_05_19_k.viewmodel.GroupGoalDetailViewModel
import java.util.*

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
    // ▼▼▼ [추가] 그룹장 여부 상태를 가져옵니다. ▼▼▼
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
                    // ▼▼▼ [수정] 그룹장일 경우에만 수정/삭제 버튼이 보이도록 합니다. ▼▼▼
                    if (isCurrentUserAdmin) {
                        val isCompleted = goalDetail?.endDate?.toDate()?.before(Date()) ?: false
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
                            // ▼▼▼ [수정] GoalDetailItem에 isEnabled 속성 전달 ▼▼▼
                            GoalDetailItem(
                                detail = detail,
                                isEnabled = isCurrentUserAdmin, // 그룹장일 때만 활성화
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
    isEnabled: Boolean, // isEnabled 파라미터 추가
    onCheckChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = detail.isCompleted,
            onCheckedChange = onCheckChange,
            enabled = isEnabled // 체크박스 활성화/비활성화 상태 설정
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = detail.description ?: "", style = MaterialTheme.typography.bodyLarge)
    }
}