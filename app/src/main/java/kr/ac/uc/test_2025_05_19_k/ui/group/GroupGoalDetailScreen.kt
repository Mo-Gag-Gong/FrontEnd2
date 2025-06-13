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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kr.ac.uc.test_2025_05_19_k.model.GoalDetailDto
import kr.ac.uc.test_2025_05_19_k.viewmodel.GroupGoalDetailViewModel
import java.util.UUID
import androidx.compose.runtime.livedata.observeAsState
import java.util.Date
import kr.ac.uc.test_2025_05_19_k.util.toDate


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
                    val isCompleted = goalDetail?.endDate?.toDate()?.before(Date()) ?: false
                    // TODO: 그룹장인 경우에만 보이도록 조건 추가 필요
                    TextButton(
                        onClick = {
                        navController.navigate("goal_edit/$groupId/$goalId")
                    },enabled = !isCompleted) {
                        Text("수정")
                    }
                    TextButton(onClick = {
                        viewModel.deleteGoal {
                            viewModel.deleteGoal {
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("should_refresh_goals", true)
                                navController.popBackStack()
                            }
                        }
                    },enabled = !isCompleted) {
                        Text("삭제")
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
    onCheckChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = detail.isCompleted,
            onCheckedChange = onCheckChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = detail.description ?: "", style = MaterialTheme.typography.bodyLarge)
    }
}