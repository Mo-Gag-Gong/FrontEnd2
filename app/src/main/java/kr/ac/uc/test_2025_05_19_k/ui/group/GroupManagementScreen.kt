package kr.ac.uc.test_2025_05_19_k.ui.group

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kr.ac.uc.test_2025_05_19_k.model.StudyGroup
import kr.ac.uc.test_2025_05_19_k.ui.common.HomeGroupCard
import kr.ac.uc.test_2025_05_19_k.viewmodel.GroupManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupManagementScreen(
    navController: NavController,
    viewModel: GroupManagementViewModel = hiltViewModel()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> viewModel.fetchMyJoinedGroups()
            1 -> viewModel.fetchMyOwnedGroups()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("스터디 그룹") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // 1. 커스텀 탭 버튼
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GroupTabButton(
                    text = "참여한 그룹",
                    isSelected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 }
                )
                GroupTabButton(
                    text = "만든 그룹",
                    isSelected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 }
                )
            }

            // 2. 탭 컨텐츠
            when (selectedTabIndex) {
                0 -> {
                    val groups by viewModel.joinedGroups.collectAsState()
                    val isLoading by viewModel.isLoadingJoined.collectAsState()
                    GroupList(
                        groups = groups,
                        isLoading = isLoading,
                        emptyMessage = "참여한 그룹이 없습니다.",
                        onGroupClick = { groupId ->
                            navController.navigate("group_detail/$groupId")
                        }
                    )
                }
                1 -> {
                    val groups by viewModel.ownedGroups.collectAsState()
                    val isLoading by viewModel.isLoadingOwned.collectAsState()
                    GroupList(
                        groups = groups,
                        isLoading = isLoading,
                        emptyMessage = "만든 그룹이 없습니다.",
                        onGroupClick = { groupId ->
                            navController.navigate("group_admin_detail/$groupId")
                        }
                    )
                }
            }
        }
    }
}

/**
 * 참여/만든 그룹 탭 버튼
 */
@Composable
private fun GroupTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
    val border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        border = border,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(text)
    }
}

/**
 * 그룹 목록을 표시하는 재사용 가능한 리스트
 */
@Composable
private fun GroupList(
    groups: List<StudyGroup>,
    isLoading: Boolean,
    emptyMessage: String,
    onGroupClick: (Long) -> Unit
) {
    when {
        isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        groups.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = emptyMessage, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        else -> {
            // ▼▼▼ [수정] LazyColumn에 간격(spacedBy)을 추가하고 Divider 제거 ▼▼▼
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(groups, key = { group -> group.groupId }) { group ->
                    HomeGroupCard(
                        group = group,
                        onClick = { onGroupClick(group.groupId) }
                    )
                }
            }
        }
    }
}